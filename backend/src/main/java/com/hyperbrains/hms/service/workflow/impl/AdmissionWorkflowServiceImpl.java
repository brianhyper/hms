package com.hyperbrains.hms.service.workflow.impl;

import com.hyperbrains.hms.domain.Admission;
import com.hyperbrains.hms.domain.AdmissionTransfer;
import com.hyperbrains.hms.domain.Bed;
import com.hyperbrains.hms.domain.Consultation;
import com.hyperbrains.hms.domain.Patient;
import com.hyperbrains.hms.domain.Prescription;
import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.domain.Visit;
import com.hyperbrains.hms.domain.Ward;
import com.hyperbrains.hms.domain.enumeration.AdmissionStatus;
import com.hyperbrains.hms.domain.enumeration.BedStatus;
import com.hyperbrains.hms.domain.enumeration.RegistrationStatus;
import com.hyperbrains.hms.domain.enumeration.VisitStatus;
import com.hyperbrains.hms.domain.enumeration.VisitType;
import com.hyperbrains.hms.repository.AdmissionRepository;
import com.hyperbrains.hms.repository.AdmissionTransferRepository;
import com.hyperbrains.hms.repository.BedRepository;
import com.hyperbrains.hms.repository.DiagnosticOrderRepository;
import com.hyperbrains.hms.repository.PrescriptionRepository;
import com.hyperbrains.hms.repository.UserRepository;
import com.hyperbrains.hms.repository.VisitRepository;
import com.hyperbrains.hms.security.SecurityUtils;
import com.hyperbrains.hms.service.AuditActions;
import com.hyperbrains.hms.service.AuditLogService;
import com.hyperbrains.hms.service.BusinessRuleViolationException;
import com.hyperbrains.hms.service.dto.view.AdmissionBedResultDTO;
import com.hyperbrains.hms.service.dto.view.AdmitPatientRequestDTO;
import com.hyperbrains.hms.service.dto.view.AssignBedRequestDTO;
import com.hyperbrains.hms.service.dto.view.TransferBedRequestDTO;
import com.hyperbrains.hms.service.dto.view.VisitAdmissionResultDTO;
import com.hyperbrains.hms.service.dto.view.WardTransferResultDTO;
import com.hyperbrains.hms.service.rules.AdmissionConversion;
import com.hyperbrains.hms.service.rules.AdmissionLifecycle;
import com.hyperbrains.hms.service.rules.BedLifecycle;
import com.hyperbrains.hms.service.rules.PrescriptionLifecycle;
import com.hyperbrains.hms.service.workflow.AdmissionWorkflowService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AdmissionWorkflowServiceImpl implements AdmissionWorkflowService {

    private static final Logger LOG = LoggerFactory.getLogger(AdmissionWorkflowServiceImpl.class);

    private final VisitRepository visitRepository;

    private final AdmissionRepository admissionRepository;

    private final BedRepository bedRepository;

    private final AdmissionTransferRepository admissionTransferRepository;

    private final DiagnosticOrderRepository diagnosticOrderRepository;

    private final PrescriptionRepository prescriptionRepository;

    private final UserRepository userRepository;

    private final AuditLogService auditLogService;

    public AdmissionWorkflowServiceImpl(
        VisitRepository visitRepository,
        AdmissionRepository admissionRepository,
        BedRepository bedRepository,
        AdmissionTransferRepository admissionTransferRepository,
        DiagnosticOrderRepository diagnosticOrderRepository,
        PrescriptionRepository prescriptionRepository,
        UserRepository userRepository,
        AuditLogService auditLogService
    ) {
        this.visitRepository = visitRepository;
        this.admissionRepository = admissionRepository;
        this.bedRepository = bedRepository;
        this.admissionTransferRepository = admissionTransferRepository;
        this.diagnosticOrderRepository = diagnosticOrderRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    @Override
    public VisitAdmissionResultDTO admit(Long visitId, AdmitPatientRequestDTO request) {
        if (request.getAdmissionReason() == null || request.getAdmissionReason().isBlank()) {
            // Checked here as well as on the DTO: bean validation only runs over HTTP, and the ground
            // for keeping a patient in is the one part of this record that cannot be reconstructed later.
            throw BusinessRuleViolationException.of(
                "admissionReasonRequired",
                "visit",
                "Admitting a patient requires a reason"
            );
        }

        Visit visit = visitRepository
            .findById(visitId)
            .orElseThrow(() -> BusinessRuleViolationException.of("visitNotFound", "visit", "No visit with id " + visitId));

        if (AdmissionConversion.isAlreadyAdmitted(visit.getType())) {
            throw BusinessRuleViolationException.of(
                "visitAlreadyAdmitted",
                "visit",
                "Visit " + visitId + " is already an admission"
            );
        }

        if (admissionRepository.existsByVisitId(visitId)) {
            // The visit's type should have caught this, so reaching here means the two disagree — which is
            // to say the type was changed by hand through a route that does not belong to this workflow. The
            // unique index on visit_id would refuse the insert anyway; this is the message rather than the
            // rule, and the rule is the database's.
            throw BusinessRuleViolationException.of(
                "admissionAlreadyExists",
                "admission",
                "Visit " + visitId + " already has a stay opened for it"
            );
        }

        if (!AdmissionConversion.canConvert(visit.getType(), visit.getStatus())) {
            throw BusinessRuleViolationException.of(
                "visitNotAdmissible",
                "visit",
                "Visit " + visitId + " is " + visit.getStatus() + " and cannot be converted to an admission"
            );
        }

        Consultation consultation = visit.getConsultation();
        if (!AdmissionConversion.hasBeenAssessed(consultation == null ? null : consultation.getStatus())) {
            // Nobody has examined this patient, so there is no clinical ground for the stay — and the
            // specification frames the action as one taken during or after a consultation.
            throw BusinessRuleViolationException.of(
                "patientNotAssessed",
                "visit",
                "Visit " + visitId + " has no consultation, so nobody has assessed this patient for admission"
            );
        }

        VisitType previousType = visit.getType();
        VisitStatus previousStatus = visit.getStatus();
        Instant admittedAt = Instant.now();

        // The heart of the specification: the type changes in place and the status parks off the
        // outpatient path. Nothing is detached, nothing is re-created, and the patient reference is
        // left alone, so vitals, consultation, orders, results and prescriptions all stay reachable
        // through the same visit they were always on.
        visit.setType(VisitType.ADMISSION);
        visit.setStatus(VisitStatus.ADMITTED);
        visit = visitRepository.save(visit);

        // The stay itself. It opens waiting for a bed rather than in one: the doctor's decision and the bed
        // being found are different moments, and treating them as one is what puts a patient in a bed that
        // was never free. The patient is reached through the visit on purpose — no second patient reference
        // is stored, so the identity merge has one fewer thing to carry.
        User admittingDoctor = currentUser();
        Admission admission = new Admission();
        admission.setVisit(visit);
        admission.setAdmittedAt(admittedAt);
        admission.setAdmissionReason(request.getAdmissionReason());
        admission.setStatus(AdmissionLifecycle.statusOnAdmission());
        admission.setAdmittingDoctor(admittingDoctor);
        admission.setPrimaryDoctor(admittingDoctor);
        admission = admissionRepository.save(admission);

        auditLogService.record(
            AuditLogService.Entry.of(AuditActions.PATIENT_ADMITTED, "Admission", admission.getId())
                .withReason(request.getAdmissionReason())
                .withField("status")
                .withChange(null, AdmissionLifecycle.statusOnAdmission().name())
                .withDetails(
                    "stay opened for visit " +
                    visit.getId() +
                    " by " +
                    admittingDoctor.getLogin() +
                    "; no bed yet, so the patient is waiting for one"
                )
        );

        LOG.info(
            "Admission {} opened for visit {} by {}, waiting for a bed",
            admission.getId(),
            visit.getId(),
            admittingDoctor.getLogin()
        );

        int ordersStillOpen = Math.toIntExact(diagnosticOrderRepository.countOutstandingByVisitId(visit.getId()));
        List<Prescription> prescriptions = prescriptionRepository.findByVisitId(visit.getId());
        int awaitingDispense = (int) prescriptions
            .stream()
            .filter(prescription -> PrescriptionLifecycle.AWAITING_PAYMENT.contains(prescription.getStatus()))
            .count();
        BigDecimal charges = visit.getBill() == null || visit.getBill().getTotalAmount() == null
            ? BigDecimal.ZERO
            : visit.getBill().getTotalAmount();

        auditLogService.record(
            AuditLogService.Entry.of(AuditActions.PATIENT_ADMITTED, "Visit", visit.getId())
                .withReason(request.getAdmissionReason())
                .withChange(previousType.name(), VisitType.ADMISSION.name())
                .withDetails(
                    "admitted from a " +
                    previousStatus +
                    " visit; consultation " +
                    (consultation == null ? "none" : consultation.getId()) +
                    "; " +
                    ordersStillOpen +
                    " order(s) still open; " +
                    awaitingDispense +
                    " prescription(s) awaiting payment; " +
                    charges +
                    " accumulated so far, to be settled under the inpatient rules"
                )
        );

        // One state move, one row: the trail is read as a story about the visit, and every other status
        // change in the system is recorded this way.
        auditLogService.record(
            AuditLogService.Entry.of(AuditActions.VISIT_STATUS_CHANGED, "Visit", visit.getId())
                .withChange(previousStatus.name(), VisitStatus.ADMITTED.name())
                .withDetails("converted to an admission, which leaves the outpatient path toward payment")
        );

        if (visit.getPatient().getRegistrationStatus() != RegistrationStatus.COMPLETE) {
            LOG.warn(
                "Visit {} admitted for patient {}, whose registration is {} — the stay is starting against an unconfirmed identity",
                visit.getId(),
                visit.getPatient().getId(),
                visit.getPatient().getRegistrationStatus()
            );
        }

        LOG.info(
            "Visit {} converted from {} to an admission: {} order(s) still open, {} prescription(s) awaiting payment, {} accumulated",
            visit.getId(),
            previousType,
            ordersStillOpen,
            awaitingDispense,
            charges
        );

        return new VisitAdmissionResultDTO(
            visit.getId(),
            admission.getId(),
            visit.getType(),
            visit.getStatus(),
            visit.getPatient().getId(),
            visit.getPatient().getHospitalId(),
            visit.getPatient().getRegistrationStatus(),
            consultation == null ? null : consultation.getId(),
            ordersStillOpen,
            awaitingDispense,
            charges,
            request.getAdmissionReason(),
            admittedAt
        );
    }

    @Override
    public AdmissionBedResultDTO assignBed(Long admissionId, AssignBedRequestDTO request) {
        if (request == null || request.getBedId() == null) {
            throw BusinessRuleViolationException.of("bedRequired", "admission", "Putting a patient into a bed requires the bed's id");
        }

        Admission admission = requireAdmission(admissionId);

        AdmissionStatus previousStatus = admission.getStatus();

        if (AdmissionLifecycle.holdsABed(previousStatus)) {
            // Refused rather than treated as "just move them": a move is a transfer, it changes who is
            // responsible for the patient, and §4 of the specification requires it to be recorded as one
            // with a reason. {@code transfer} is that action.
            throw BusinessRuleViolationException.of(
                "admissionAlreadyInABed",
                "admission",
                "Admission " +
                admissionId +
                " is already in bed " +
                (admission.getBed() == null ? "?" : admission.getBed().getBedNumber()) +
                "; moving a patient between beds is a ward transfer and has to be recorded as one"
            );
        }
        if (!AdmissionLifecycle.awaitsABed(previousStatus)) {
            throw BusinessRuleViolationException.of(
                "admissionNotAwaitingBed",
                "admission",
                "Admission " + admissionId + " is " + previousStatus + ", so it cannot be given a bed"
            );
        }

        Bed bed = requireBed(request.getBedId());
        requireBedUsable(bed);

        Patient patient = patientOf(admission);
        Long patientId = patient == null ? null : patient.getId();
        requireNoOtherOpenStay(admission, patient);

        Ward ward = bed.getWard();
        BedStatus previousBedStatus = bed.getStatus();
        bed.setStatus(BedStatus.OCCUPIED);
        bed = bedRepository.save(bed);

        admission.setBed(bed);
        admission.setStatus(AdmissionLifecycle.statusAfterBedAssigned());
        admission = admissionRepository.save(admission);
        Instant assignedAt = Instant.now();

        // Three entries, because three different questions get asked afterwards and each is a query over one
        // entity: what has this bed been through, when did this patient arrive in it, and how did the stay's
        // status get where it is.
        auditLogService.record(
            AuditLogService.Entry.of(AuditActions.BED_STATUS_CHANGED, "Bed", bed.getId())
                .withField("status")
                .withChange(previousBedStatus.name(), BedStatus.OCCUPIED.name())
                .withDetails(
                    "bed " +
                    describe(bed) +
                    " taken by admission " +
                    admission.getId() +
                    " for patient " +
                    (patient == null ? "unknown" : patient.getHospitalId())
                )
        );
        auditLogService.record(
            AuditLogService.Entry.of(AuditActions.BED_ASSIGNED, "Admission", admission.getId())
                .withField("bed")
                .withChange(null, bed.getBedNumber())
                .withDetails("patient placed in bed " + describe(bed))
        );
        auditLogService.record(
            AuditLogService.Entry.of(AuditActions.ADMISSION_STATUS_CHANGED, "Admission", admission.getId())
                .withField("status")
                .withChange(previousStatus.name(), AdmissionStatus.ADMITTED.name())
                .withDetails("a bed was found, so the stay is now properly under way")
        );

        LOG.info(
            "Admission {} given bed {} in ward {}; bed moved from {} to {}",
            admission.getId(),
            bed.getBedNumber(),
            ward.getName(),
            previousBedStatus,
            bed.getStatus()
        );
        return new AdmissionBedResultDTO(
            admission.getId(),
            previousStatus,
            admission.getStatus(),
            bed.getId(),
            bed.getBedNumber(),
            ward.getId(),
            ward.getName(),
            patientId,
            patient == null ? null : patient.getHospitalId(),
            assignedAt
        );
    }

    @Override
    public WardTransferResultDTO transfer(Long admissionId, TransferBedRequestDTO request) {
        if (request == null || request.getReason() == null || request.getReason().isBlank()) {
            // Checked here as well as on the DTO: bean validation only runs over HTTP, and a move with no
            // reason recorded is a move nobody can account for when the patient's whereabouts are questioned.
            throw BusinessRuleViolationException.of(
                "transferReasonRequired",
                "admission",
                "Moving a patient to another bed requires a reason"
            );
        }
        if (request.getToBedId() == null) {
            throw BusinessRuleViolationException.of(
                "bedRequired",
                "admission",
                "Moving a patient requires the bed they are moving to"
            );
        }

        Admission admission = requireAdmission(admissionId);
        AdmissionStatus status = admission.getStatus();

        if (!AdmissionLifecycle.isOpen(status)) {
            throw BusinessRuleViolationException.of(
                "admissionNotOpen",
                "admission",
                "Admission " + admissionId + " is " + status + " and cannot be moved anywhere"
            );
        }

        Bed fromBed = admission.getBed();
        if (fromBed == null) {
            // Covers a stay that is still waiting for a bed, and a record whose status claims a bed it does
            // not have. Neither has anywhere to be moved from.
            throw BusinessRuleViolationException.of(
                "admissionNotInABed",
                "admission",
                "Admission " + admissionId + " is not in a bed, so there is nothing to move it from; give it a bed instead"
            );
        }

        Bed toBed = requireBed(request.getToBedId());
        if (toBed.getId().equals(fromBed.getId())) {
            throw BusinessRuleViolationException.of(
                "bedUnchanged",
                "admission",
                "The patient is already in bed " + describe(fromBed) + ", so there is nothing to move"
            );
        }
        requireBedUsable(toBed);

        BedStatus previousFromStatus = fromBed.getStatus();
        BedStatus previousToStatus = toBed.getStatus();

        // The bed being left is never returned straight to AVAILABLE: it has just been occupied, and the same
        // rule as a discharge applies — somebody has to say it is clean before the next patient goes in it.
        fromBed.setStatus(BedLifecycle.statusAfterVacating());
        fromBed = bedRepository.save(fromBed);

        toBed.setStatus(BedStatus.OCCUPIED);
        toBed = bedRepository.save(toBed);

        // The admission's bed is the only record of where the patient is, so the ward a doctor is judged
        // against moves with them. Nothing caches it.
        admission.setBed(toBed);
        admission = admissionRepository.save(admission);

        User transferredBy = currentUser();
        Instant transferredAt = Instant.now();

        AdmissionTransfer transfer = new AdmissionTransfer();
        transfer.setAdmission(admission);
        transfer.setFromBed(fromBed);
        transfer.setToBed(toBed);
        transfer.setTransferredBy(transferredBy);
        transfer.setTransferredAt(transferredAt);
        transfer.setReason(request.getReason());
        transfer = admissionTransferRepository.save(transfer);

        Patient patient = patientOf(admission);
        String patientLabel = patient == null ? "unknown" : patient.getHospitalId();

        auditLogService.record(
            AuditLogService.Entry.of(AuditActions.WARD_TRANSFERRED, "Admission", admission.getId())
                .withReason(request.getReason())
                .withField("bed")
                .withChange(describe(fromBed), describe(toBed))
                .withDetails(
                    "patient " + patientLabel + " moved from ward " + wardName(fromBed) + " to ward " + wardName(toBed)
                )
        );
        auditLogService.record(
            AuditLogService.Entry.of(AuditActions.BED_STATUS_CHANGED, "Bed", fromBed.getId())
                .withField("status")
                .withChange(previousFromStatus.name(), fromBed.getStatus().name())
                .withDetails("bed " + describe(fromBed) + " vacated by a transfer; it needs cleaning before the next patient")
        );
        auditLogService.record(
            AuditLogService.Entry.of(AuditActions.BED_STATUS_CHANGED, "Bed", toBed.getId())
                .withField("status")
                .withChange(previousToStatus.name(), toBed.getStatus().name())
                .withDetails(
                    "bed " + describe(toBed) + " taken by admission " + admission.getId() + " for patient " + patientLabel
                )
        );

        LOG.info(
            "Admission {} moved from bed {} (ward {}) to bed {} (ward {}) by {}: {}",
            admission.getId(),
            fromBed.getBedNumber(),
            wardName(fromBed),
            toBed.getBedNumber(),
            wardName(toBed),
            transferredBy.getLogin(),
            request.getReason()
        );

        return new WardTransferResultDTO(
            transfer.getId(),
            admission.getId(),
            fromBed.getId(),
            fromBed.getBedNumber(),
            fromBed.getWard() == null ? null : fromBed.getWard().getId(),
            wardName(fromBed),
            toBed.getId(),
            toBed.getBedNumber(),
            toBed.getWard() == null ? null : toBed.getWard().getId(),
            wardName(toBed),
            request.getReason(),
            transferredAt,
            patient == null ? null : patient.getId(),
            patient == null ? null : patient.getHospitalId()
        );
    }

    private Admission requireAdmission(Long admissionId) {
        return admissionRepository
            .findById(admissionId)
            .orElseThrow(() ->
                BusinessRuleViolationException.of("admissionNotFound", "admission", "No admission with id " + admissionId)
            );
    }

    private Bed requireBed(Long bedId) {
        return bedRepository
            .findById(bedId)
            .orElseThrow(() -> BusinessRuleViolationException.of("bedNotFound", "bed", "No bed with id " + bedId));
    }

    /**
     * A bed may only be given to a patient if it is free and its ward is still taking patients.
     *
     * <p>Shared by assignment and transfer, because they are the same question asked at two moments, and a
     * rule written twice is a rule that will eventually be enforced once.
     */
    private static void requireBedUsable(Bed bed) {
        Ward ward = bed.getWard();
        if (ward == null || !Boolean.TRUE.equals(ward.getActive())) {
            // The availability query already hides these, so this is the same rule stated where it cannot be
            // bypassed: a bed in a ward that has stopped taking patients is not a bed.
            throw BusinessRuleViolationException.of(
                "bedInClosedWard",
                "bed",
                "Bed " + describe(bed) + " is in a ward that is not taking patients"
            );
        }
        if (!BedLifecycle.isAssignable(bed.getStatus())) {
            String reason = BedLifecycle.isHoldingPatient(bed.getStatus())
                ? "already holds a patient"
                : "is " + bed.getStatus() + " and cannot be given to a patient";
            throw BusinessRuleViolationException.of("bedNotAvailable", "bed", "Bed " + describe(bed) + " " + reason);
        }
    }

    /**
     * Refuses a second open stay for the same patient.
     *
     * <p>This cannot be a database index: the patient is reached through the visit — {@code Admission} stores
     * no patient reference, deliberately, so the identity merge has one fewer thing to carry — and a partial
     * unique index cannot join. So the rule lives here, and the assertions in the tests are what hold it.
     */
    private void requireNoOtherOpenStay(Admission admission, Patient patient) {
        if (patient == null || patient.getId() == null) {
            return;
        }
        long otherOpenStays = admissionRepository.countByVisitPatientIdAndStatusNotAndIdNot(
            patient.getId(),
            AdmissionStatus.DISCHARGED,
            admission.getId()
        );
        if (otherOpenStays > 0) {
            throw BusinessRuleViolationException.of(
                "patientAlreadyAdmitted",
                "admission",
                "Patient " + patient.getHospitalId() + " already has " + otherOpenStays + " stay(s) open"
            );
        }
    }

    private static Patient patientOf(Admission admission) {
        Visit visit = admission.getVisit();
        return visit == null ? null : visit.getPatient();
    }

    private static String wardName(Bed bed) {
        Ward ward = bed.getWard();
        return ward == null ? "none" : ward.getName();
    }

    /**
     * The clinician performing the action. Taken from the authenticated principal, never from the request —
     * a client-supplied doctor would make the clinical record unverifiable.
     */
    private User currentUser() {
        String login = SecurityUtils
            .getCurrentUserLogin()
            .orElseThrow(() ->
                BusinessRuleViolationException.of("authenticationRequired", "admission", "No authenticated user in scope")
            );
        return userRepository
            .findOneByLogin(login)
            .orElseThrow(() -> BusinessRuleViolationException.of("unknownUser", "admission", "No user account for " + login));
    }

    private static String describe(Bed bed) {
        Ward ward = bed.getWard();
        return bed.getBedNumber() + " in ward " + (ward == null ? "none" : ward.getName());
    }
}
