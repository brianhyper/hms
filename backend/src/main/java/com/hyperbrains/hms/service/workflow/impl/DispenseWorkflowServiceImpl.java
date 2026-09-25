package com.hyperbrains.hms.service.workflow.impl;

import com.hyperbrains.hms.domain.Dispense;
import com.hyperbrains.hms.domain.DispenseLine;
import com.hyperbrains.hms.domain.Prescription;
import com.hyperbrains.hms.domain.PrescriptionLine;
import com.hyperbrains.hms.domain.User;
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
import com.hyperbrains.hms.service.PharmacyStockService;
import com.hyperbrains.hms.service.dto.view.DispenseLineRequestDTO;
import com.hyperbrains.hms.service.dto.view.DispenseRecordDTO;
import com.hyperbrains.hms.service.dto.view.DispenseRequestDTO;
import com.hyperbrains.hms.service.dto.view.PrescriptionViewDTO;
import com.hyperbrains.hms.service.rules.DispenseProgress;
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

    public DispenseWorkflowServiceImpl(
        PrescriptionRepository prescriptionRepository,
        PrescriptionLineRepository prescriptionLineRepository,
        DispenseRepository dispenseRepository,
        DispenseLineRepository dispenseLineRepository,
        UserRepository userRepository,
        PharmacyStockService pharmacyStockService,
        AuditLogService auditLogService
    ) {
        this.prescriptionRepository = prescriptionRepository;
        this.prescriptionLineRepository = prescriptionLineRepository;
        this.dispenseRepository = dispenseRepository;
        this.dispenseLineRepository = dispenseLineRepository;
        this.userRepository = userRepository;
        this.pharmacyStockService = pharmacyStockService;
        this.auditLogService = auditLogService;
    }

    @Override
    public PrescriptionViewDTO dispense(Long prescriptionId, DispenseRequestDTO request) {
        Prescription prescription = loadPrescription(prescriptionId);

        if (!PrescriptionLifecycle.isDispensable(prescription.getStatus())) {
            // One message covering both reasons, because both mean the same thing to the counter. Naming
            // only the bill would send a pharmacist looking for a payment problem on a prescription that
            // has been withdrawn.
            throw BusinessRuleViolationException.of(
                "prescriptionNotReadyForDispense",
                "prescription",
                "Prescription " +
                prescriptionId +
                " is " +
                prescription.getStatus() +
                " and cannot be handed over. Medicine is released only once its bill is settled, and a withdrawn prescription is never released"
            );
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
                            line.getDrug().getName(),
                            line.getDrug().getUnit(),
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
