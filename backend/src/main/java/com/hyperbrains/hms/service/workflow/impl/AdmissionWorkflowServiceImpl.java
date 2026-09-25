package com.hyperbrains.hms.service.workflow.impl;

import com.hyperbrains.hms.domain.Consultation;
import com.hyperbrains.hms.domain.Prescription;
import com.hyperbrains.hms.domain.Visit;
import com.hyperbrains.hms.domain.enumeration.RegistrationStatus;
import com.hyperbrains.hms.domain.enumeration.VisitStatus;
import com.hyperbrains.hms.domain.enumeration.VisitType;
import com.hyperbrains.hms.repository.DiagnosticOrderRepository;
import com.hyperbrains.hms.repository.PrescriptionRepository;
import com.hyperbrains.hms.repository.VisitRepository;
import com.hyperbrains.hms.service.AuditActions;
import com.hyperbrains.hms.service.AuditLogService;
import com.hyperbrains.hms.service.BusinessRuleViolationException;
import com.hyperbrains.hms.service.dto.view.AdmitPatientRequestDTO;
import com.hyperbrains.hms.service.dto.view.VisitAdmissionResultDTO;
import com.hyperbrains.hms.service.rules.AdmissionConversion;
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

    private final DiagnosticOrderRepository diagnosticOrderRepository;

    private final PrescriptionRepository prescriptionRepository;

    private final AuditLogService auditLogService;

    public AdmissionWorkflowServiceImpl(
        VisitRepository visitRepository,
        DiagnosticOrderRepository diagnosticOrderRepository,
        PrescriptionRepository prescriptionRepository,
        AuditLogService auditLogService
    ) {
        this.visitRepository = visitRepository;
        this.diagnosticOrderRepository = diagnosticOrderRepository;
        this.prescriptionRepository = prescriptionRepository;
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
}
