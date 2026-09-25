package com.hyperbrains.hms.service.workflow.impl;

import com.hyperbrains.hms.config.HmsProperties;
import com.hyperbrains.hms.domain.Department;
import com.hyperbrains.hms.domain.Patient;
import com.hyperbrains.hms.domain.Referral;
import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.domain.Visit;
import com.hyperbrains.hms.domain.enumeration.ReferralStatus;
import com.hyperbrains.hms.domain.enumeration.VisitStatus;
import com.hyperbrains.hms.repository.DepartmentRepository;
import com.hyperbrains.hms.repository.ReferralRepository;
import com.hyperbrains.hms.repository.UserRepository;
import com.hyperbrains.hms.repository.VisitRepository;
import com.hyperbrains.hms.security.SecurityUtils;
import com.hyperbrains.hms.service.AuditActions;
import com.hyperbrains.hms.service.AuditLogService;
import com.hyperbrains.hms.service.BusinessRuleViolationException;
import com.hyperbrains.hms.service.MailService;
import com.hyperbrains.hms.service.dto.view.CreateReferralRequestDTO;
import com.hyperbrains.hms.service.dto.view.ReferralViewDTO;
import com.hyperbrains.hms.service.report.ReferralLetterRenderer;
import com.hyperbrains.hms.service.workflow.ReferralWorkflowService;
import com.hyperbrains.hms.service.workflow.VisitStatusService;
import jakarta.mail.MessagingException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReferralWorkflowServiceImpl implements ReferralWorkflowService {

    private static final Logger LOG = LoggerFactory.getLogger(ReferralWorkflowServiceImpl.class);

    private static final String PDF_CONTENT_TYPE = "application/pdf";

    private static final DateTimeFormatter LETTER_DATE = DateTimeFormatter.ofPattern("d MMMM yyyy");

    /**
     * The statuses from which the local journey can be ended.
     *
     * <p>Everything from the consultation being finished onwards. Referring <em>during</em> a
     * consultation is deliberately not allowed: it would move the visit to the payment stage without the
     * consultation being completed, so the fee would never be raised and the notes would stay
     * unfinished. A doctor who wants to refer immediately completes the consultation first — which they
     * need to do anyway for the fee to be billed.
     */
    private static final Set<VisitStatus> REFERRABLE_STATUSES = Set.of(
        VisitStatus.WAITING_RESULTS,
        VisitStatus.WAITING_PAYMENT,
        VisitStatus.ADMITTED
    );

    private final VisitRepository visitRepository;

    private final ReferralRepository referralRepository;

    private final DepartmentRepository departmentRepository;

    private final UserRepository userRepository;

    private final MailService mailService;

    private final VisitStatusService visitStatusService;

    private final AuditLogService auditLogService;

    private final HmsProperties properties;

    public ReferralWorkflowServiceImpl(
        VisitRepository visitRepository,
        ReferralRepository referralRepository,
        DepartmentRepository departmentRepository,
        UserRepository userRepository,
        MailService mailService,
        VisitStatusService visitStatusService,
        AuditLogService auditLogService,
        HmsProperties properties
    ) {
        this.visitRepository = visitRepository;
        this.referralRepository = referralRepository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.mailService = mailService;
        this.visitStatusService = visitStatusService;
        this.auditLogService = auditLogService;
        this.properties = properties;
    }

    @Override
    public ReferralViewDTO create(Long visitId, CreateReferralRequestDTO request) {
        Visit visit = loadVisit(visitId);

        if (!REFERRABLE_STATUSES.contains(visit.getStatus())) {
            throw BusinessRuleViolationException.of(
                "visitNotReferrable",
                "visit",
                "Visit " + visitId + " is " + visit.getStatus() +
                ". Complete the consultation before referring, so the fee is billed and the notes are finished"
            );
        }

        Referral referral = new Referral();
        referral.setVisit(visit);
        referral.setReferredBy(currentUser());
        referral.setType(request.getType());
        referral.setDestination(request.getDestination());
        referral.setDestinationEmail(request.getDestinationEmail());
        referral.setReason(request.getReason());
        referral.setNotes(request.getNotes());
        // PENDING means written but not yet dispatched. It becomes COMPLETED when the letter actually
        // reaches the destination, not when someone opens it on screen.
        referral.setStatus(ReferralStatus.PENDING);
        referral.setCreatedAt(Instant.now());
        if (request.getDepartmentId() != null) {
            referral.setDepartment(loadDepartment(request.getDepartmentId()));
        }
        referral = referralRepository.save(referral);

        auditLogService.record(
            AuditLogService.Entry.of(AuditActions.REFERRAL_CREATED, "Referral", referral.getId()).withDetails(
                "%s referral to %s".formatted(request.getType(), request.getDestination())
            )
        );

        // The clinical half of a referral: this hospital's local journey is over, so outstanding tests no
        // longer hold the visit back.
        visitStatusService.onReferralCreated(visit.getId());

        LOG.debug("Referral {} created for visit {}", referral.getId(), visit.getId());
        return view(referral);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReferralViewDTO> forVisit(Long visitId) {
        return referralRepository.findByVisitIdOrderByCreatedAt(visitId).stream().map(ReferralViewDTO::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RenderedDocument renderLetter(Long referralId) {
        Referral referral = loadReferral(referralId);
        return new RenderedDocument(letterFilename(referral), PDF_CONTENT_TYPE, renderPdf(referral));
    }

    @Override
    public ReferralViewDTO emailLetter(Long referralId) {
        Referral referral = loadReferral(referralId);
        if (isBlank(referral.getDestinationEmail())) {
            throw BusinessRuleViolationException.of(
                "referralHasNoDestinationEmail",
                "referral",
                "Referral " + referralId + " has no destination email address, so the letter cannot be sent"
            );
        }

        byte[] letter = renderPdf(referral);
        Patient patient = referral.getVisit() == null ? null : referral.getVisit().getPatient();

        try {
            mailService.sendEmailWithAttachment(
                referral.getDestinationEmail(),
                "Referral letter: %s".formatted(patient == null ? "patient" : patient.getFullName()),
                """
                Please find the referral letter for this patient attached.

                If you are unable to open the attachment, please contact us quoting the patient's \
                hospital number.""",
                letter,
                letterFilename(referral),
                PDF_CONTENT_TYPE
            );
        } catch (MessagingException | MailException e) {
            // Left PENDING and surfaced, rather than marked delivered. A referral that looks sent but
            // never arrived means a patient turned away at the receiving facility, and nobody here would
            // know to chase it.
            LOG.warn("Referral letter {} could not be emailed", referralId, e);
            throw new IllegalStateException("The referral letter could not be emailed; the referral is still pending", e);
        }

        referral.setStatus(ReferralStatus.COMPLETED);
        referral = referralRepository.save(referral);

        auditLogService.record(
            AuditLogService.Entry.of(AuditActions.REFERRAL_LETTER_EMAILED, "Referral", referral.getId()).withDetails(
                "Letter sent to " + referral.getDestinationEmail()
            )
        );

        LOG.debug("Referral letter {} emailed to {}", referralId, referral.getDestinationEmail());
        return view(referral);
    }

    private byte[] renderPdf(Referral referral) {
        // Built per call from configuration so the letterhead reflects the current setting without a
        // restart having been forgotten about.
        ReferralLetterRenderer renderer = new ReferralLetterRenderer(properties.getReferral().getFacilityName());

        Visit visit = referral.getVisit();
        Patient patient = visit == null ? null : visit.getPatient();

        ReferralLetterRenderer.ReferralLetter letter = new ReferralLetterRenderer.ReferralLetter(
            patient == null ? null : patient.getFullName(),
            patient == null ? null : patient.getHospitalId(),
            patient == null || patient.getSex() == null ? null : patient.getSex().name(),
            patient == null || patient.getDateOfBirth() == null ? null : patient.getDateOfBirth().toString(),
            referral.getDestination(),
            referral.getDepartment() == null ? null : referral.getDepartment().getName(),
            referral.getReason(),
            referral.getNotes(),
            referral.getReferredBy() == null ? "unknown" : referral.getReferredBy().getLogin(),
            LocalDate.now(hospitalZone()).format(LETTER_DATE)
        );

        return renderer.render(letter);
    }

    /**
     * The hospital's local zone.
     *
     * <p>Read from the appointments zone, which is the only place the hospital's wall clock is
     * configured. A server running in UTC would otherwise date a letter written just after midnight as
     * yesterday, and that goes on a document sent to another facility.
     */
    private ZoneId hospitalZone() {
        return ZoneId.of(properties.getAppointments().getZone());
    }

    private String letterFilename(Referral referral) {
        Patient patient = referral.getVisit() == null ? null : referral.getVisit().getPatient();
        String identifier = patient == null || isBlank(patient.getHospitalId()) ? "patient" : patient.getHospitalId();
        // Anything that could not appear safely in a filename is dropped rather than escaped.
        return "referral-%s-%d.pdf".formatted(identifier.replaceAll("[^A-Za-z0-9-]", ""), referral.getId());
    }

    private ReferralViewDTO view(Referral referral) {
        return ReferralViewDTO.from(referral);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private Referral loadReferral(Long referralId) {
        return referralRepository
            .findById(referralId)
            .orElseThrow(() -> BusinessRuleViolationException.of("referralNotFound", "referral", "No referral with id " + referralId));
    }

    private Department loadDepartment(Long departmentId) {
        return departmentRepository
            .findById(departmentId)
            .orElseThrow(() ->
                BusinessRuleViolationException.of("departmentNotFound", "department", "No department with id " + departmentId)
            );
    }

    private Visit loadVisit(Long visitId) {
        return visitRepository
            .findById(visitId)
            .orElseThrow(() -> BusinessRuleViolationException.of("visitNotFound", "visit", "No visit with id " + visitId));
    }

    private User currentUser() {
        String login = SecurityUtils
            .getCurrentUserLogin()
            .orElseThrow(() ->
                BusinessRuleViolationException.of("authenticationRequired", "referral", "No authenticated user in scope")
            );
        return userRepository
            .findOneByLogin(login)
            .orElseThrow(() -> BusinessRuleViolationException.of("unknownUser", "referral", "No user account for " + login));
    }
}
