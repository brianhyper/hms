package com.hyperbrains.hms.service.workflow.impl;

import com.hyperbrains.hms.domain.Bed;
import com.hyperbrains.hms.domain.Ward;
import com.hyperbrains.hms.domain.enumeration.BedStatus;
import com.hyperbrains.hms.repository.BedRepository;
import com.hyperbrains.hms.service.AuditActions;
import com.hyperbrains.hms.service.AuditLogService;
import com.hyperbrains.hms.service.BusinessRuleViolationException;
import com.hyperbrains.hms.service.dto.view.BedStatusViewDTO;
import com.hyperbrains.hms.service.dto.view.MarkBedMaintenanceRequestDTO;
import com.hyperbrains.hms.service.rules.BedLifecycle;
import com.hyperbrains.hms.service.workflow.BedWorkflowService;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The hand-made transitions of a bed's status, and nothing else.
 *
 * <p>Assignment — {@code AVAILABLE → OCCUPIED} — deliberately does not live here. It is not a status
 * edit, it is a consequence of putting a patient in the bed, and it will be performed by the admission
 * service that owns both halves of that change.
 */
@Service
@Transactional
public class BedWorkflowServiceImpl implements BedWorkflowService {

    private static final Logger LOG = LoggerFactory.getLogger(BedWorkflowServiceImpl.class);

    private final BedRepository bedRepository;

    private final AuditLogService auditLogService;

    public BedWorkflowServiceImpl(BedRepository bedRepository, AuditLogService auditLogService) {
        this.bedRepository = bedRepository;
        this.auditLogService = auditLogService;
    }

    @Override
    public BedStatusViewDTO markAvailable(Long bedId) {
        Bed bed = requireBed(bedId);
        BedStatus previous = bed.getStatus();

        if (previous == BedStatus.AVAILABLE) {
            // Not treated as a quiet success. Either somebody else released this bed first, or this is a
            // second click on the same button, and in both cases the caller should learn that it did not
            // change the bed rather than be told it did.
            throw BusinessRuleViolationException.of("bedAlreadyAvailable", "bed", "Bed " + describe(bed) + " is already available");
        }
        if (BedLifecycle.isHoldingPatient(previous)) {
            throw BusinessRuleViolationException.of(
                "bedHoldsPatient",
                "bed",
                "Bed " + describe(bed) + " is occupied: it cannot be released until the patient in it has left"
            );
        }
        if (!BedLifecycle.canTransition(previous, BedStatus.AVAILABLE)) {
            throw BusinessRuleViolationException.of(
                "bedStatusChangeNotAllowed",
                "bed",
                "Bed " + describe(bed) + " is " + previous + ", which cannot be made available"
            );
        }

        return change(bed, previous, BedStatus.AVAILABLE, null);
    }

    @Override
    public BedStatusViewDTO markMaintenance(Long bedId, MarkBedMaintenanceRequestDTO request) {
        if (request == null || request.getReason() == null || request.getReason().isBlank()) {
            // Checked here as well as on the DTO: bean validation only runs over HTTP, and a bed taken out
            // of service with no reason recorded is a bed nobody can account for when somebody asks why the
            // ward has lost a bed.
            throw BusinessRuleViolationException.of(
                "maintenanceReasonRequired",
                "bed",
                "Taking a bed out of service requires a reason"
            );
        }

        Bed bed = requireBed(bedId);
        BedStatus previous = bed.getStatus();

        if (previous == BedStatus.MAINTENANCE) {
            throw BusinessRuleViolationException.of(
                "bedAlreadyInMaintenance",
                "bed",
                "Bed " + describe(bed) + " is already out of service"
            );
        }
        if (BedLifecycle.isHoldingPatient(previous)) {
            throw BusinessRuleViolationException.of(
                "bedHoldsPatient",
                "bed",
                "Bed " + describe(bed) + " is occupied: the patient has to leave it before it can go out of service"
            );
        }
        if (!BedLifecycle.canTransition(previous, BedStatus.MAINTENANCE)) {
            throw BusinessRuleViolationException.of(
                "bedStatusChangeNotAllowed",
                "bed",
                "Bed " + describe(bed) + " is " + previous + ", which cannot be taken out of service"
            );
        }

        return change(bed, previous, BedStatus.MAINTENANCE, request.getReason());
    }

    /**
     * Applies one permitted change, records it, and reports what happened.
     *
     * <p>One row in the audit trail per move, keyed on the bed, so the question "what has this bed been
     * through" is a query rather than a reconstruction.
     */
    private BedStatusViewDTO change(Bed bed, BedStatus from, BedStatus to, String reason) {
        bed.setStatus(to);
        Bed saved = bedRepository.save(bed);
        Instant changedAt = Instant.now();

        AuditLogService.Entry entry = AuditLogService.Entry.of(AuditActions.BED_STATUS_CHANGED, "Bed", saved.getId())
            .withField("status")
            .withChange(from.name(), to.name());
        if (reason != null) {
            entry = entry.withReason(reason);
        }
        entry = entry.withDetails(
            "bed " + saved.getBedNumber() + " in ward " + wardName(saved) + (reason == null ? "" : "; " + reason)
        );
        auditLogService.record(entry);

        LOG.info("Bed {} in ward {} moved from {} to {}", saved.getBedNumber(), wardName(saved), from, to);

        return new BedStatusViewDTO(
            saved.getId(),
            saved.getBedNumber(),
            saved.getWard() == null ? null : saved.getWard().getId(),
            wardName(saved),
            from,
            saved.getStatus(),
            changedAt
        );
    }

    private Bed requireBed(Long bedId) {
        return bedRepository
            .findById(bedId)
            .orElseThrow(() -> BusinessRuleViolationException.of("bedNotFound", "bed", "No bed with id " + bedId));
    }

    private static String wardName(Bed bed) {
        Ward ward = bed.getWard();
        return ward == null ? "none" : ward.getName();
    }

    private static String describe(Bed bed) {
        return bed.getBedNumber() + " in ward " + wardName(bed);
    }
}
