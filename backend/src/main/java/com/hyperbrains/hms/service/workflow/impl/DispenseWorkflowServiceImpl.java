package com.hyperbrains.hms.service.workflow.impl;

import com.hyperbrains.hms.domain.Dispense;
import com.hyperbrains.hms.domain.DispenseLine;
import com.hyperbrains.hms.domain.Prescription;
import com.hyperbrains.hms.domain.PrescriptionLine;
import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.domain.Visit;
import com.hyperbrains.hms.domain.enumeration.PrescriptionStatus;
import com.hyperbrains.hms.repository.DispenseLineRepository;
import com.hyperbrains.hms.repository.DispenseRepository;
import com.hyperbrains.hms.repository.PrescriptionLineRepository;
import com.hyperbrains.hms.repository.PrescriptionRepository;
import com.hyperbrains.hms.repository.UserRepository;
import com.hyperbrains.hms.security.SecurityUtils;
import com.hyperbrains.hms.service.AuditActions;
import com.hyperbrains.hms.service.AuditLogService;
import com.hyperbrains.hms.service.BusinessRuleViolationException;
import com.hyperbrains.hms.service.OverrideService;
import com.hyperbrains.hms.service.PharmacyStockService;
import com.hyperbrains.hms.service.dto.view.DispenseLineRequestDTO;
import com.hyperbrains.hms.service.dto.view.DispenseRecordDTO;
import com.hyperbrains.hms.service.dto.view.DispenseRequestDTO;
import com.hyperbrains.hms.service.dto.view.OverrideRequestDTO;
import com.hyperbrains.hms.service.dto.view.PrescriptionViewDTO;
import com.hyperbrains.hms.service.rules.BreakGlass;
import com.hyperbrains.hms.service.rules.DispenseProgress;
import com.hyperbrains.hms.service.rules.DrugSnapshot;
import com.hyperbrains.hms.service.rules.PrescriptionLifecycle;
import com.hyperbrains.hms.service.workflow.DispenseWorkflowService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DispenseWorkflowServiceImpl implements DispenseWorkflowService {

    private static final Logger LOG = LoggerFactory.getLogger(DispenseWorkflowServiceImpl.class);

    private final PrescriptionRepository prescriptionRepository;

    private final PrescriptionLineRepository prescriptionLineRepository;

    private final DispenseRepository dispenseRepository;

    private final DispenseLineRepository dispenseLineRepository;

    private final UserRepository userRepository;

    private final PharmacyStockService pharmacyStockService;

    private final AuditLogService auditLogService;

    private final OverrideService overrideService;

    public DispenseWorkflowServiceImpl(
        PrescriptionRepository prescriptionRepository,
        PrescriptionLineRepository prescriptionLineRepository,
        DispenseRepository dispenseRepository,
        DispenseLineRepository dispenseLineRepository,
        UserRepository userRepository,
        PharmacyStockService pharmacyStockService,
        AuditLogService auditLogService,
        OverrideService overrideService
    ) {
        this.prescriptionRepository = prescriptionRepository;
        this.prescriptionLineRepository = prescriptionLineRepository;
        this.dispenseRepository = dispenseRepository;
        this.dispenseLineRepository = dispenseLineRepository;
        this.userRepository = userRepository;
        this.pharmacyStockService = pharmacyStockService;
        this.auditLogService = auditLogService;
        this.overrideService = overrideService;
    }

    @Override
    public PrescriptionViewDTO dispense(Long prescriptionId, DispenseRequestDTO request) {
        Prescription prescription = loadPrescription(prescriptionId);

        if (!PrescriptionLifecycle.isDispensable(prescription.getStatus())) {
            if (!PrescriptionLifecycle.AWAITING_PAYMENT.contains(prescription.getStatus())) {
                // Withdrawn, or already handed over: the medicine is not owed at the counter, so an override cannot
                // help. Break-glass releases what is still behind the payment gate and nothing else.
                throw notReadyForDispense(prescription);
            }
            releaseBeforePayment(prescription, request);
        }

        List<PrescriptionLine> orderedLines = prescriptionLineRepository.findWithDrugByPrescriptionId(prescriptionId);
        Map<Long, PrescriptionLine> linesById = orderedLines
            .stream()
            .collect(Collectors.toMap(PrescriptionLine::getId, line -> line));

        // Validate everything before moving any stock. A partly-applied hand-over would leave the shelf
        // short and the record disagreeing with it.
        Map<Long, Integer> dispensedSoFar = new LinkedHashMap<>();
        Set<Long> seenInThisRequest = new HashSet<>();
        for (DispenseLineRequestDTO requested : request.getLines()) {
            PrescriptionLine line = linesById.get(requested.getPrescriptionLineId());
            if (line == null) {
                throw BusinessRuleViolationException.of(
                    "dispenseLineNotOnPrescription",
                    "prescription",
                    "Prescription line " + requested.getPrescriptionLineId() + " does not belong to prescription " + prescriptionId
                );
            }
            if (!seenInThisRequest.add(line.getId())) {
                // Two entries for one line could each fit inside the remaining quantity while together
                // exceeding it, which would hand over more medicine than was ever prescribed.
                throw BusinessRuleViolationException.of(
                    "dispenseLineRepeated",
                    "prescription",
                    "Prescription line " + line.getId() + " appears more than once in the same hand-over"
                );
            }

            int dispensed = (int) dispenseLineRepository.sumQuantityByPrescriptionLineId(line.getId());
            if (DispenseProgress.exceedsRemaining(line.getQuantity(), dispensed, requested.getQuantity())) {
                throw BusinessRuleViolationException.of(
                    "dispenseExceedsRemaining",
                    "prescription",
                    "%s: %d %s still outstanding but %d requested".formatted(
                        line.getDrug().getName(),
                        DispenseProgress.remaining(line.getQuantity(), dispensed),
                        line.getDrug().getUnit(),
                        requested.getQuantity()
                    )
                );
            }
            dispensedSoFar.put(line.getId(), dispensed);
        }

        User dispenser = currentUser();
        Dispense dispense = new Dispense();
        dispense.setPrescription(prescription);
        dispense.setRecordedBy(dispenser);
        dispense.setDispensedAt(Instant.now());
        dispense.setNote(request.getNote());
        dispense = dispenseRepository.save(dispense);

        for (DispenseLineRequestDTO requested : request.getLines()) {
            PrescriptionLine line = linesById.get(requested.getPrescriptionLineId());

            // Reserved units become shelf movement only now: currentStock falls and the promise against
            // those units is discharged in the same step.
            pharmacyStockService.consume(line.getDrug().getId(), requested.getQuantity());

            DispenseLine dispenseLine = new DispenseLine();
            dispenseLine.setDispense(dispense);
            dispenseLine.setPrescriptionLine(line);
            // No substitution in Phase 1: what is handed over is what was prescribed.
            dispenseLine.setDrug(line.getDrug());
            // And what that drug was at this moment, kept on the line: the price and the class can change afterwards,
            // and a hand-over has to stay readable as what was actually given.
            DrugSnapshot.onto(dispenseLine, line.getDrug());
            dispenseLine.setQuantity(requested.getQuantity());
            dispenseLineRepository.save(dispenseLine);

            dispensedSoFar.merge(line.getId(), requested.getQuantity(), Integer::sum);
        }

        PrescriptionStatus previous = prescription.getStatus();
        prescription.setStatus(
            DispenseProgress.statusFor(
                orderedLines
                    .stream()
                    .map(line -> DispenseProgress.remaining(line.getQuantity(), dispensedSoFar.getOrDefault(line.getId(), 0)))
                    .toList()
            )
        );
        prescription = prescriptionRepository.save(prescription);

        auditLogService.record(
            AuditLogService.Entry.of(AuditActions.PRESCRIPTION_DISPENSED, "Prescription", prescription.getId())
                .withChange(previous.name(), prescription.getStatus().name())
                .withDetails("%d line(s) handed over on dispense %d".formatted(request.getLines().size(), dispense.getId()))
        );

        LOG.debug("Dispense {} recorded for prescription {}; status now {}", dispense.getId(), prescription.getId(), prescription.getStatus());
        return PrescriptionViewDTO.from(prescription, orderedLines);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DispenseRecordDTO> history(Long prescriptionId) {
        loadPrescription(prescriptionId);

        // Grouped from one query rather than a query per hand-over; the ordering already comes from the
        // database, and LinkedHashMap keeps it.
        Map<Long, List<DispenseLine>> byDispense = dispenseLineRepository
            .findWithDrugByPrescriptionId(prescriptionId)
            .stream()
            .collect(
                Collectors.groupingBy(line -> line.getDispense().getId(), LinkedHashMap::new, Collectors.toCollection(ArrayList::new))
            );

        return byDispense
            .values()
            .stream()
            .map(lines -> {
                Dispense dispense = lines.getFirst().getDispense();
                List<DispenseRecordDTO.DispensedItemDTO> items = lines
                    .stream()
                    .map(line ->
                        new DispenseRecordDTO.DispensedItemDTO(
                            line.getPrescriptionLine().getId(),
                            // What was handed over, at the price and name that applied then, rather than what the
                            // catalogue entry says now — this is the history of a hand-over that happened.
                            DrugSnapshot.nameToShow(line),
                            DrugSnapshot.unitToShow(line),
                            line.getQuantity(),
                            line.getSubstitutionReason()
                        )
                    )
                    .toList();
                return DispenseRecordDTO.of(
                    dispense.getId(),
                    dispense.getDispensedAt(),
                    dispense.getRecordedBy() == null ? null : dispense.getRecordedBy().getLogin(),
                    dispense.getNote(),
                    items
                );
            })
            .toList();
    }

    /**
     * Break-glass: medicine released before the bill is settled, for a patient in the confirmed scope, by a pharmacist
     * or doctor at the point of care, with a reason, recorded as its own audit event. The bill is deliberately left
     * alone, so it stays outstanding and is still collected — this is a receivable, not a write-off.
     *
     * <p>Refused with the ordinary message when the caller is not one of the two roles or the visit is out of scope;
     * refused with its own message when a reason is all that is missing, so the counter knows what to supply. A
     * withdrawn prescription never reaches here.
     */
    private void releaseBeforePayment(Prescription prescription, DispenseRequestDTO request) {
        Visit visit = prescription.getVisit();
        boolean inScope = BreakGlass.isInScope(visit.getPriority(), visit.getType());
        boolean mayInvoke = BreakGlass.mayBeInvokedBy(SecurityUtils.getCurrentUserAuthorities());

        if (!(inScope && mayInvoke)) {
            throw notReadyForDispense(prescription);
        }
        if (request.getOverrideReason() == null || request.getOverrideReason().isBlank()) {
            throw BusinessRuleViolationException.of(
                "emergencyReleaseReasonRequired",
                "prescription",
                "Releasing medicine before the bill is settled is an override, so it must say why; supply overrideReason."
            );
        }

        OverrideRequestDTO override = new OverrideRequestDTO();
        override.setOverriddenEntity("Prescription");
        override.setOverriddenEntityId(String.valueOf(prescription.getId()));
        override.setReason(request.getOverrideReason());
        overrideService.record(override);
    }

    /** One message covering both reasons, because both mean the same thing to the counter. */
    private static BusinessRuleViolationException notReadyForDispense(Prescription prescription) {
        return BusinessRuleViolationException.of(
            "prescriptionNotReadyForDispense",
            "prescription",
            "Prescription " +
            prescription.getId() +
            " is " +
            prescription.getStatus() +
            " and cannot be handed over. Medicine is released only once its bill is settled, and a withdrawn prescription is never released"
        );
    }

    private Prescription loadPrescription(Long prescriptionId) {
        return prescriptionRepository
            .findById(prescriptionId)
            .orElseThrow(() ->
                BusinessRuleViolationException.of("prescriptionNotFound", "prescription", "No prescription with id " + prescriptionId)
            );
    }

    /** Who handed it over, taken from the authenticated principal and never from the request. */
    private User currentUser() {
        String login = SecurityUtils
            .getCurrentUserLogin()
            .orElseThrow(() ->
                BusinessRuleViolationException.of("authenticationRequired", "dispense", "No authenticated user in scope")
            );
        return userRepository
            .findOneByLogin(login)
            .orElseThrow(() -> BusinessRuleViolationException.of("unknownUser", "dispense", "No user account for " + login));
    }
}
