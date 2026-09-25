package com.hyperbrains.hms.service.workflow.impl;

import com.hyperbrains.hms.domain.Appointment;
import com.hyperbrains.hms.domain.Patient;
import com.hyperbrains.hms.domain.Visit;
import com.hyperbrains.hms.domain.enumeration.AppointmentStatus;
import com.hyperbrains.hms.domain.enumeration.RegistrationStatus;
import com.hyperbrains.hms.domain.enumeration.VisitPriority;
import com.hyperbrains.hms.domain.enumeration.VisitType;
import com.hyperbrains.hms.repository.AppointmentRepository;
import com.hyperbrains.hms.repository.PatientRepository;
import com.hyperbrains.hms.repository.VisitRepository;
import com.hyperbrains.hms.service.AuditActions;
import com.hyperbrains.hms.service.AuditLogService;
import com.hyperbrains.hms.service.BusinessRuleViolationException;
import com.hyperbrains.hms.service.dto.VisitDTO;
import com.hyperbrains.hms.service.dto.view.AppointmentCheckInRequestDTO;
import com.hyperbrains.hms.service.dto.view.VisitIntakeRequestDTO;
import com.hyperbrains.hms.service.mapper.VisitMapper;
import com.hyperbrains.hms.service.rules.VisitLifecycle;
import com.hyperbrains.hms.service.workflow.VisitIntakeService;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class VisitIntakeServiceImpl implements VisitIntakeService {

    private static final Logger LOG = LoggerFactory.getLogger(VisitIntakeServiceImpl.class);

    /**
     * {@code Visit.reasonForVisit} is {@code NOT NULL}, but an appointment's reason is optional. A
     * booked slot with no stated reason is normal, so the desk is not forced to invent one.
     */
    private static final String FALLBACK_REASON = "Appointment check-in";

    private final AppointmentRepository appointmentRepository;

    private final PatientRepository patientRepository;

    private final VisitRepository visitRepository;

    private final VisitMapper visitMapper;

    private final AuditLogService auditLogService;

    public VisitIntakeServiceImpl(
        AppointmentRepository appointmentRepository,
        PatientRepository patientRepository,
        VisitRepository visitRepository,
        VisitMapper visitMapper,
        AuditLogService auditLogService
    ) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.visitRepository = visitRepository;
        this.visitMapper = visitMapper;
        this.auditLogService = auditLogService;
    }

    @Override
    public VisitDTO checkIn(Long appointmentId, AppointmentCheckInRequestDTO request) {
        Appointment appointment = appointmentRepository
            .findById(appointmentId)
            .orElseThrow(() ->
                BusinessRuleViolationException.of("appointmentNotFound", "appointment", "No appointment with id " + appointmentId)
            );

        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            // This is the rule that keeps a missed appointment final. Reviving it here would
            // retroactively attach a late arrival to a slot the hospital already wrote off, and
            // the patient would be billed and queued as if they had turned up on time.
            throw BusinessRuleViolationException.of(
                "appointmentNotCheckable",
                "appointment",
                "Only a scheduled appointment can be checked in; this one is " + appointment.getStatus()
            );
        }

        Patient patient = appointment.getPatient();
        if (patient == null) {
            throw BusinessRuleViolationException.of(
                "appointmentWithoutPatient",
                "appointment",
                "Appointment " + appointmentId + " has no patient"
            );
        }

        Visit visit = new Visit();
        visit.setPatient(patient);
        visit.setType(VisitType.OUTPATIENT);
        visit.setPriority(request.getPriority() == null ? VisitPriority.NORMAL : request.getPriority());
        visit.setReasonForVisit(reasonFor(visitReasonOrNull(request), appointment.getReason()));
        visit.setStatus(VisitLifecycle.initialStatus(VisitType.OUTPATIENT));
        visit.setCreatedAt(Instant.now());
        visit = visitRepository.save(visit);

        appointment.setStatus(AppointmentStatus.CHECKED_IN);
        appointment.setVisit(visit);
        appointmentRepository.save(appointment);

        auditLogService.record(
            AuditLogService.Entry.of(AuditActions.APPOINTMENT_CHECKED_IN, "Appointment", appointment.getId()).withDetails(
                "Visit " + visit.getId() + " opened"
            )
        );
        auditLogService.record(
            AuditLogService.Entry.of(AuditActions.VISIT_CREATED, "Visit", visit.getId()).withDetails(
                "From appointment " + appointment.getId()
            )
        );
        LOG.debug("Checked in appointment {} as visit {}", appointment.getId(), visit.getId());

        return visitMapper.toDto(visit);
    }

    @Override
    public VisitDTO createVisit(VisitIntakeRequestDTO request) {
        Patient patient = patientRepository
            .findById(request.getPatientId())
            .orElseThrow(() ->
                BusinessRuleViolationException.of("patientNotFound", "visit", "No patient with id " + request.getPatientId())
            );

        if (patient.getRegistrationStatus() == RegistrationStatus.MERGED) {
            // A merged record has been superseded. Opening clinical work against it would create
            // data that the merge will never see and that nobody will ever look at again.
            throw BusinessRuleViolationException.of(
                "patientMerged",
                "visit",
                "Patient " + patient.getHospitalId() + " was merged into another record and cannot start a new visit"
            );
        }

        VisitType type = request.getType();
        if (type == null) {
            throw BusinessRuleViolationException.of("visitTypeRequired", "visit", "A visit type is required to open a visit");
        }
        if (type == VisitType.ADMISSION) {
            throw BusinessRuleViolationException.of(
                "visitTypeNotOpenable",
                "visit",
                "Admission is reached by converting an existing visit, not by opening a new one"
            );
        }

        Visit visit = new Visit();
        visit.setPatient(patient);
        visit.setType(type);
        visit.setPriority(request.getPriority() == null ? VisitPriority.NORMAL : request.getPriority());
        visit.setReasonForVisit(request.getReasonForVisit());
        visit.setStatus(VisitLifecycle.initialStatus(type));
        visit.setCreatedAt(Instant.now());
        visit = visitRepository.save(visit);

        auditLogService.record(
            AuditLogService.Entry.of(AuditActions.VISIT_CREATED, "Visit", visit.getId()).withDetails(
                type + " visit for patient " + patient.getHospitalId()
            )
        );
        LOG.debug("Opened {} visit {} for patient {}", type, visit.getId(), patient.getHospitalId());

        return visitMapper.toDto(visit);
    }

    private static String visitReasonOrNull(AppointmentCheckInRequestDTO request) {
        return request.getReasonForVisit() == null || request.getReasonForVisit().isBlank() ? null : request.getReasonForVisit();
    }

    private static String reasonFor(String preferred, String fallback) {
        if (preferred != null) {
            return preferred;
        }
        return fallback == null || fallback.isBlank() ? FALLBACK_REASON : fallback;
    }
}
