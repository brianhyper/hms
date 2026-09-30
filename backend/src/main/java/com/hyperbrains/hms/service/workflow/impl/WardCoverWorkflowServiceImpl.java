package com.hyperbrains.hms.service.workflow.impl;

import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.domain.Ward;
import com.hyperbrains.hms.domain.WardCover;
import com.hyperbrains.hms.repository.UserRepository;
import com.hyperbrains.hms.repository.WardCoverRepository;
import com.hyperbrains.hms.repository.WardRepository;
import com.hyperbrains.hms.security.AuthoritiesConstants;
import com.hyperbrains.hms.security.SecurityUtils;
import com.hyperbrains.hms.service.AuditActions;
import com.hyperbrains.hms.service.AuditLogService;
import com.hyperbrains.hms.service.BusinessRuleViolationException;
import com.hyperbrains.hms.service.PersonNames;
import com.hyperbrains.hms.service.dto.view.AssignWardCoverRequestDTO;
import com.hyperbrains.hms.service.dto.view.WardCoverViewDTO;
import com.hyperbrains.hms.service.rules.WardCoverage;
import com.hyperbrains.hms.service.workflow.WardCoverWorkflowService;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The duty roster, and the two ways a roster entry can be wrong.
 */
@Service
@Transactional
public class WardCoverWorkflowServiceImpl implements WardCoverWorkflowService {

    private static final Logger LOG = LoggerFactory.getLogger(WardCoverWorkflowServiceImpl.class);

    private final WardCoverRepository wardCoverRepository;

    private final WardRepository wardRepository;

    private final UserRepository userRepository;

    private final AuditLogService auditLogService;

    public WardCoverWorkflowServiceImpl(
        WardCoverRepository wardCoverRepository,
        WardRepository wardRepository,
        UserRepository userRepository,
        AuditLogService auditLogService
    ) {
        this.wardCoverRepository = wardCoverRepository;
        this.wardRepository = wardRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    @Override
    public WardCoverViewDTO assign(AssignWardCoverRequestDTO request) {
        if (request == null || request.getDoctorId() == null || request.getWardId() == null || request.getCoversFrom() == null) {
            throw BusinessRuleViolationException.of(
                "coverDetailsRequired",
                "wardCover",
                "Putting a doctor on duty for a ward needs the doctor, the ward and when it starts"
            );
        }
        if (!WardCoverage.isWellFormed(request.getCoversFrom(), request.getCoversTo())) {
            // Refused rather than stored: a period that ends before it starts looks like cover on a roster
            // and grants nothing, so a doctor would be told they are on duty and see no patients.
            throw BusinessRuleViolationException.of(
                "coverPeriodNotWellFormed",
                "wardCover",
                "A period of cover has to end after it starts; leave the end out for cover with no known end"
            );
        }

        User doctor = userRepository
            .findById(request.getDoctorId())
            .orElseThrow(() ->
                BusinessRuleViolationException.of("unknownUser", "wardCover", "No user with id " + request.getDoctorId())
            );
        Ward ward = wardRepository
            .findById(request.getWardId())
            .orElseThrow(() ->
                BusinessRuleViolationException.of("wardNotFound", "wardCover", "No ward with id " + request.getWardId())
            );

        if (!Boolean.TRUE.equals(ward.getActive())) {
            throw BusinessRuleViolationException.of(
                "wardNotActive",
                "wardCover",
                "Ward " + ward.getName() + " is not taking patients, so covering it would mean nothing"
            );
        }
        boolean isADoctor = doctor.getAuthorities().stream().anyMatch(authority -> AuthoritiesConstants.DOCTOR.equals(authority.getName()));
        if (!isADoctor) {
            // The roster's only job is to decide which doctors see which patients. An entry naming somebody
            // else is a silent no-op: the ward looks covered and nobody is looking after it.
            throw BusinessRuleViolationException.of(
                "notADoctor",
                "wardCover",
                doctor.getLogin() + " is not a doctor, so the roster would grant nothing"
            );
        }

        User assignedBy = currentUser();
        Instant now = Instant.now();

        WardCover cover = new WardCover();
        cover.setDoctor(doctor);
        cover.setWard(ward);
        cover.setCoversFrom(request.getCoversFrom());
        cover.setCoversTo(request.getCoversTo());
        cover.setNote(request.getNote());
        cover.setAssignedBy(assignedBy);
        cover = wardCoverRepository.save(cover);

        auditLogService.record(
            AuditLogService.Entry.of(AuditActions.WARD_COVER_CHANGED, "WardCover", cover.getId())
                .withField("coversFrom")
                .withChange(null, request.getCoversFrom().toString())
                .withDetails(
                    describe(cover) + "; assigned by " + assignedBy.getLogin() + describeEnd(request.getCoversTo())
                )
        );

        LOG.info("Ward cover {} assigned: {}", cover.getId(), describe(cover));

        return view(cover, now);
    }

    @Override
    public WardCoverViewDTO end(Long coverId) {
        WardCover cover = wardCoverRepository
            .findById(coverId)
            .orElseThrow(() -> BusinessRuleViolationException.of("coverNotFound", "wardCover", "No cover with id " + coverId));

        Instant now = Instant.now();
        if (WardCoverage.hasEndedBy(cover.getCoversTo(), now)) {
            throw BusinessRuleViolationException.of(
                "coverAlreadyEnded",
                "wardCover",
                "This cover ended at " + cover.getCoversTo() + ", so there is nothing to end"
            );
        }

        Instant planned = cover.getCoversTo();
        cover.setCoversTo(now);
        cover = wardCoverRepository.save(cover);

        auditLogService.record(
            AuditLogService.Entry.of(AuditActions.WARD_COVER_CHANGED, "WardCover", cover.getId())
                .withField("coversTo")
                .withChange(planned == null ? "open-ended" : planned.toString(), now.toString())
                .withDetails(describe(cover) + "; ended early")
        );

        LOG.info("Ward cover {} ended at {}", cover.getId(), now);

        return view(cover, now);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WardCoverViewDTO> current() {
        Instant now = Instant.now();
        return wardCoverRepository
            .findAllWithWard()
            .stream()
            .filter(cover -> WardCoverage.isActiveAt(cover.getCoversFrom(), cover.getCoversTo(), now))
            .map(cover -> view(cover, now))
            .toList();
    }

    private WardCoverViewDTO view(WardCover cover, Instant now) {
        return new WardCoverViewDTO(
            cover.getId(),
            cover.getDoctor().getId(),
            PersonNames.displayName(cover.getDoctor()),
            cover.getWard().getId(),
            cover.getWard().getName(),
            cover.getCoversFrom(),
            cover.getCoversTo(),
            cover.getNote(),
            cover.getAssignedBy() == null ? null : cover.getAssignedBy().getId(),
            PersonNames.displayName(cover.getAssignedBy()),
            WardCoverage.isActiveAt(cover.getCoversFrom(), cover.getCoversTo(), now)
        );
    }

    private static String describe(WardCover cover) {
        return cover.getDoctor().getLogin() + " covering " + cover.getWard().getName();
    }

    private static String describeEnd(Instant coversTo) {
        return coversTo == null ? ", open-ended" : ", until " + coversTo;
    }

    private User currentUser() {
        String login = SecurityUtils
            .getCurrentUserLogin()
            .orElseThrow(() -> BusinessRuleViolationException.of("authenticationRequired", "wardCover", "No authenticated user in scope"));
        return userRepository
            .findOneByLogin(login)
            .orElseThrow(() -> BusinessRuleViolationException.of("unknownUser", "wardCover", "No user account for " + login));
    }
}
