package com.hyperbrains.hms.service.workflow.impl;

import com.hyperbrains.hms.domain.Bill;
import com.hyperbrains.hms.domain.Drug;
import com.hyperbrains.hms.domain.Prescription;
import com.hyperbrains.hms.domain.PrescriptionLine;
import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.domain.Visit;
import com.hyperbrains.hms.domain.enumeration.BillLineSourceType;
import com.hyperbrains.hms.domain.enumeration.PrescriptionSource;
import com.hyperbrains.hms.domain.enumeration.PrescriptionStatus;
import com.hyperbrains.hms.domain.enumeration.VisitStatus;
import com.hyperbrains.hms.repository.AdmissionRepository;
import com.hyperbrains.hms.repository.PrescriptionLineRepository;
import com.hyperbrains.hms.repository.PrescriptionRepository;
import com.hyperbrains.hms.repository.UserRepository;
import com.hyperbrains.hms.repository.VisitRepository;
import com.hyperbrains.hms.security.SecurityUtils;
import com.hyperbrains.hms.service.AuditActions;
import com.hyperbrains.hms.service.AuditLogService;
import com.hyperbrains.hms.service.BusinessRuleViolationException;
import com.hyperbrains.hms.service.PharmacyStockService;
import com.hyperbrains.hms.service.dto.view.PlacePrescriptionRequestDTO;
import com.hyperbrains.hms.service.dto.view.PrescriptionBillableDTO;
import com.hyperbrains.hms.service.dto.view.PrescriptionLineRequestDTO;
import com.hyperbrains.hms.service.dto.view.PrescriptionViewDTO;
import com.hyperbrains.hms.service.rules.AdmissionLifecycle;
import com.hyperbrains.hms.service.rules.DrugSnapshot;
import com.hyperbrains.hms.service.rules.PrescriptionLifecycle;
import com.hyperbrains.hms.service.rules.VisitLifecycle;
import com.hyperbrains.hms.service.workflow.BillingService;
import com.hyperbrains.hms.service.workflow.PrescriptionWorkflowService;
import com.hyperbrains.hms.service.workflow.VisitStatusService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PrescriptionWorkflowServiceImpl implements PrescriptionWorkflowService {

    private static final Logger LOG = LoggerFactory.getLogger(PrescriptionWorkflowServiceImpl.class);

    private final VisitRepository visitRepository;

    private final AdmissionRepository admissionRepository;

    private final PrescriptionRepository prescriptionRepository;

    private final PrescriptionLineRepository prescriptionLineRepository;

    private final UserRepository userRepository;

    private final PharmacyStockService pharmacyStockService;

    private final BillingService billingService;

    private final VisitStatusService visitStatusService;

    private final AuditLogService auditLogService;

    public PrescriptionWorkflowServiceImpl(
        VisitRepository visitRepository,
        AdmissionRepository admissionRepository,
        PrescriptionRepository prescriptionRepository,
        PrescriptionLineRepository prescriptionLineRepository,
        UserRepository userRepository,
        PharmacyStockService pharmacyStockService,
        BillingService billingService,
        VisitStatusService visitStatusService,
        AuditLogService auditLogService
    ) {
        this.visitRepository = visitRepository;
        this.admissionRepository = admissionRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.prescriptionLineRepository = prescriptionLineRepository;
        this.userRepository = userRepository;
        this.pharmacyStockService = pharmacyStockService;
        this.billingService = billingService;
        this.visitStatusService = visitStatusService;
        this.auditLogService = auditLogService;
    }

    @Override
    public PrescriptionViewDTO place(Long visitId, PlacePrescriptionRequestDTO request) {
        return place(loadOpenVisit(visitId), request, false);
    }

    @Override
    public PrescriptionViewDTO placeForInpatient(Long visitId, PlacePrescriptionRequestDTO request) {
        return place(loadVisitWithAnOpenStay(visitId), request, true);
    }

    private PrescriptionViewDTO place(Visit visit, PlacePrescriptionRequestDTO request, boolean inpatient) {
        // Re-checked here as well as on the DTO. Bean validation only runs when the request comes in
        // over HTTP, and this is the method that decides whether medicine gets set aside, so it must
        // not depend on having been called from a controller.
        if (
            request.getSource() == PrescriptionSource.EXTERNAL &&
            (request.getPrescribingSource() == null || request.getPrescribingSource().isBlank())
        ) {
            throw BusinessRuleViolationException.of(
                "prescribingSourceRequired",
                "prescription",
                "An outside prescription must name the prescriber or clinic it came from"
            );
        }

        // An internal prescription belongs to the doctor who wrote it, taken from the authenticated
        // principal rather than the payload. An outside one has no doctor of ours to attribute it to,
        // which is exactly why the request has to name the prescriber.
        User doctor = request.getSource() == PrescriptionSource.INTERNAL ? currentUser() : null;

        // Reserve first. If any line cannot be honoured, the transaction rolls back every reservation
        // already taken for this prescription — the patient gets a prescription that is fully backed
        // or none at all.
        List<Drug> reservedDrugs = new ArrayList<>(request.getLines().size());
        for (PrescriptionLineRequestDTO line : request.getLines()) {
            reservedDrugs.add(pharmacyStockService.reserve(line.getDrugId(), line.getQuantity()));
        }

        Prescription prescription = new Prescription();
        prescription.setVisit(visit);
        prescription.setDoctor(doctor);
        prescription.setSource(request.getSource());
        prescription.setPrescribingSource(request.getPrescribingSource());
        prescription.setStatus(
            inpatient
                ? PrescriptionLifecycle.initialStatusForInpatient()
                : PrescriptionLifecycle.initialStatus(visit.getStatus() == VisitStatus.WAITING_PAYMENT)
        );
        prescription.setCreatedAt(Instant.now());
        prescription = prescriptionRepository.save(prescription);

        List<PrescriptionLine> lines = new ArrayList<>(request.getLines().size());
        for (int i = 0; i < request.getLines().size(); i++) {
            PrescriptionLineRequestDTO requested = request.getLines().get(i);
            PrescriptionLine line = new PrescriptionLine();
            line.setPrescription(prescription);
            line.setDrug(reservedDrugs.get(i));
            // What the drug is now, kept on the line for as long as the line exists: the catalogue entry can be
            // renamed or repriced, and this prescription still has to say what was prescribed.
            DrugSnapshot.onto(line, reservedDrugs.get(i));
            line.setDosage(requested.getDosage());
            line.setDuration(requested.getDuration());
            line.setQuantity(requested.getQuantity());
            lines.add(line);
        }
        lines = prescriptionLineRepository.saveAll(lines);

        chargeForPrescription(visit, lines);

        auditLogService.record(
            AuditLogService.Entry.of(AuditActions.PRESCRIPTION_PLACED, "Prescription", prescription.getId()).withDetails(
                "%d line(s) for visit %d, source %s, stock reserved".formatted(lines.size(), visit.getId(), request.getSource())
            )
        );

        if (!inpatient) {
            // The outpatient status machine only. An admitted visit is parked off that path deliberately, and
            // pushing it from here would send a patient in a bed back into a waiting room.
            visitStatusService.onPrescriptionPlaced(visit.getId());
        }

        LOG.debug("Prescription {} placed on visit {} with {} line(s)", prescription.getId(), visit.getId(), lines.size());
        return PrescriptionViewDTO.from(prescription, lines);
    }

    @Override
    public PrescriptionViewDTO cancel(Long prescriptionId, String reason) {
        if (reason == null || reason.isBlank()) {
            // Stock coming back and money coming off a bill are both material movements. Without a
            // stated cause the audit trail cannot answer why the patient's bill shrank.
            throw BusinessRuleViolationException.of(
                "cancellationReasonRequired",
                "prescription",
                "Withdrawing a prescription requires a reason"
            );
        }

        Prescription prescription = loadPrescription(prescriptionId);

        // An inpatient prescription is dispensable from the moment it is written, because the ward collects
        // medicine without paying at a desk. Nothing has been handed over while it is still READY_FOR_DISPENSE,
        // and nothing has been paid for it either, so withdrawing it is the same act as it is at the outpatient
        // desk — the medicine goes back on the shelf and off the stay's bill.
        boolean onUnpaidCredit =
            PrescriptionLifecycle.isCancellableOnUnpaidCredit(prescription.getStatus()) &&
            hasOpenStay(prescription.getVisit());

        if (!PrescriptionLifecycle.isCancellable(prescription.getStatus()) && !onUnpaidCredit) {
            throw BusinessRuleViolationException.of(
                "prescriptionNotCancellable",
                "prescription",
                "Prescription " + prescriptionId + " is " + prescription.getStatus() + " and cannot be withdrawn. " +
                "Once it is paid the medicine is owed, and undoing that is a refund"
            );
        }

        List<PrescriptionLine> lines = prescriptionLineRepository.findWithDrugByPrescriptionId(prescriptionId);
        Visit visit = prescription.getVisit();

        // Give the stock back before touching the money, so a failure anywhere leaves the shelf and
        // the reservation consistent with each other.
        for (PrescriptionLine line : lines) {
            pharmacyStockService.release(line.getDrug().getId(), line.getQuantity());
        }

        if (visit != null && visit.getBill() != null) {
            for (PrescriptionLine line : lines) {
                billingService.removeLine(visit.getBill(), BillLineSourceType.PHARMACY, line.getId());
            }
        }

        PrescriptionStatus previous = prescription.getStatus();
        prescription.setStatus(PrescriptionStatus.CANCELLED);
        prescription = prescriptionRepository.save(prescription);

        auditLogService.record(
            AuditLogService.Entry.of(AuditActions.PRESCRIPTION_CANCELLED, "Prescription", prescription.getId())
                .withReason(reason)
                .withChange(previous.name(), PrescriptionStatus.CANCELLED.name())
                .withDetails(lines.size() + " line(s) released from stock and voided from the bill")
        );

        if (visit != null && !onUnpaidCredit) {
            // The outpatient status machine only, for the same reason placement skips it: an admitted visit is
            // governed by the stay, and recomputing its status from outpatient work would send a patient in a
            // bed back into a waiting room.
            visitStatusService.onPrescriptionWithdrawn(visit.getId());
        }

        LOG.debug("Prescription {} withdrawn: {} line(s) released", prescription.getId(), lines.size());
        return view(prescription);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionViewDTO> forVisit(Long visitId) {
        List<Prescription> prescriptions = prescriptionRepository.findByVisitId(visitId);
        if (prescriptions.isEmpty()) {
            return List.of();
        }
        Map<Long, List<PrescriptionLine>> linesByPrescription = linesGroupedByPrescription(
            prescriptions.stream().map(Prescription::getId).toList()
        );
        return prescriptions
            .stream()
            .map(prescription -> PrescriptionViewDTO.from(prescription, linesByPrescription.get(prescription.getId())))
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PrescriptionViewDTO> pharmacyQueue(Pageable pageable) {
        Page<Prescription> page = prescriptionRepository.findQueue(PrescriptionLifecycle.DISPENSABLE, pageable);
        if (page.isEmpty()) {
            return page.map(prescription -> PrescriptionViewDTO.from(prescription, List.of()));
        }

        // One query for every line on the page rather than one per prescription.
        Map<Long, List<PrescriptionLine>> linesByPrescription = linesGroupedByPrescription(
            page.getContent().stream().map(Prescription::getId).toList()
        );
        List<PrescriptionViewDTO> content = page
            .getContent()
            .stream()
            .map(prescription -> PrescriptionViewDTO.from(prescription, linesByPrescription.get(prescription.getId())))
            .toList();
        return new PageImpl<>(content, pageable, page.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionBillableDTO> billableForVisit(Long visitId) {
        return prescriptionLineRepository
            .findWithDrugByVisitId(visitId)
            .stream()
            .map(line -> PrescriptionBillableDTO.from(line, line.getPrescription().getId(), visitId))
            .toList();
    }

    @Override
    public PrescriptionViewDTO markPaid(Long prescriptionId) {
        Prescription prescription = loadPrescription(prescriptionId);

        if (!PrescriptionLifecycle.canBePaid(prescription.getStatus())) {
            // Already handed over or already queued: the payment step is retried, so arriving twice is
            // normal and must not fail.
            LOG.debug("Prescription {} is {} and needs no payment transition", prescriptionId, prescription.getStatus());
            return view(prescription);
        }

        PrescriptionStatus previous = prescription.getStatus();
        prescription.setStatus(PrescriptionLifecycle.afterPayment(previous));
        prescription = prescriptionRepository.save(prescription);

        auditLogService.record(
            AuditLogService.Entry.of(AuditActions.PRESCRIPTION_READY_FOR_DISPENSE, "Prescription", prescription.getId())
                .withChange(previous.name(), prescription.getStatus().name())
                .withDetails("Bill settled, released to the pharmacy dispensing queue")
        );

        LOG.debug("Prescription {} released for dispensing ({})", prescription.getId(), prescription.getStatus());
        return view(prescription);
    }

    /**
     * Charge for medicine line by line, keyed on the prescription line.
     *
     * <p>Per line rather than per prescription so the idempotency key
     * ({@code PHARMACY:<lineId>}) is as narrow as the thing being charged. A retry updates the same
     * line; two different drugs never share a key.
     */
    private void chargeForPrescription(Visit visit, List<PrescriptionLine> lines) {
        Bill bill = billingService.ensureBill(visit);
        for (PrescriptionLine line : lines) {
            Drug drug = line.getDrug();
            BigDecimal amount = drug.getPrice().multiply(BigDecimal.valueOf(line.getQuantity()));
            billingService.addOrUpdateLine(
                bill,
                BillLineSourceType.PHARMACY,
                BillingService.sourceRef(BillLineSourceType.PHARMACY, line.getId()),
                drug.getName(),
                amount
            );
        }
    }

    private Map<Long, List<PrescriptionLine>> linesGroupedByPrescription(List<Long> prescriptionIds) {
        return prescriptionLineRepository
            .findWithDrugByPrescriptionIdIn(prescriptionIds)
            .stream()
            .collect(
                Collectors.groupingBy(line -> line.getPrescription().getId(), LinkedHashMap::new, Collectors.toCollection(ArrayList::new))
            );
    }

    private PrescriptionViewDTO view(Prescription prescription) {
        return PrescriptionViewDTO.from(prescription, prescriptionLineRepository.findWithDrugByPrescriptionId(prescription.getId()));
    }

    private Prescription loadPrescription(Long prescriptionId) {
        return prescriptionRepository
            .findById(prescriptionId)
            .orElseThrow(() ->
                BusinessRuleViolationException.of("prescriptionNotFound", "prescription", "No prescription with id " + prescriptionId)
            );
    }

    private Visit loadOpenVisit(Long visitId) {
        Visit visit = visitRepository
            .findById(visitId)
            .orElseThrow(() -> BusinessRuleViolationException.of("visitNotFound", "visit", "No visit with id " + visitId));
        if (!VisitLifecycle.isOpen(visit.getStatus())) {
            throw BusinessRuleViolationException.of(
                "visitNotOpen",
                "visit",
                "Visit " + visitId + " is " + visit.getStatus() + " and no longer accepts clinical work"
            );
        }
        return visit;
    }

    /** Whether this visit is a stay still in progress, which is what makes the ward's credit apply. */
    private boolean hasOpenStay(Visit visit) {
        if (visit == null || visit.getId() == null) {
            return false;
        }
        return admissionRepository
            .findByVisitId(visit.getId())
            .map(admission -> AdmissionLifecycle.isOpen(admission.getStatus()))
            .orElse(false);
    }

    /**
     * The visit a prescription may be written against when the patient is an inpatient: one whose stay is
     * still open.
     *
     * <p>The guard is the Admission, not the visit status. {@code VisitStatus.ADMITTED} means the encounter
     * has left the outpatient path, which is exactly why {@link #loadOpenVisit} refuses it — so asking the
     * visit whether it accepts clinical work would refuse every inpatient. The stay is what says whether the
     * patient is still in the building.
     */
    private Visit loadVisitWithAnOpenStay(Long visitId) {
        Visit visit = visitRepository
            .findById(visitId)
            .orElseThrow(() -> BusinessRuleViolationException.of("visitNotFound", "visit", "No visit with id " + visitId));
        admissionRepository
            .findByVisitId(visitId)
            .filter(admission -> AdmissionLifecycle.isOpen(admission.getStatus()))
            .orElseThrow(() ->
                BusinessRuleViolationException.of(
                    "stayNotOpen",
                    "visit",
                    "Visit " + visitId + " has no stay in progress, so it is not being prescribed for as an inpatient"
                )
            );
        return visit;
    }

    /** The prescriber, taken from the authenticated principal and never from the request. */
    private User currentUser() {
        String login = SecurityUtils
            .getCurrentUserLogin()
            .orElseThrow(() ->
                BusinessRuleViolationException.of("authenticationRequired", "prescription", "No authenticated user in scope")
            );
        return userRepository
            .findOneByLogin(login)
            .orElseThrow(() -> BusinessRuleViolationException.of("unknownUser", "prescription", "No user account for " + login));
    }
}
