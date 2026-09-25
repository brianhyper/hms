package com.hyperbrains.hms.service.impl;

import com.hyperbrains.hms.domain.Appointment;
import com.hyperbrains.hms.domain.Patient;
import com.hyperbrains.hms.domain.Visit;
import com.hyperbrains.hms.domain.enumeration.RegistrationStatus;
import com.hyperbrains.hms.repository.AppointmentRepository;
import com.hyperbrains.hms.repository.PatientRepository;
import com.hyperbrains.hms.repository.VisitRepository;
import com.hyperbrains.hms.service.AuditActions;
import com.hyperbrains.hms.service.AuditLogService;
import com.hyperbrains.hms.service.BusinessRuleViolationException;
import com.hyperbrains.hms.service.PatientMergeService;
import com.hyperbrains.hms.service.dto.view.MergePatientRequestDTO;
import com.hyperbrains.hms.service.dto.view.PatientMergeResultDTO;
import com.hyperbrains.hms.service.rules.PatientMerge;
import com.hyperbrains.hms.service.rules.VisitLifecycle;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PatientMergeServiceImpl implements PatientMergeService {

    private static final Logger LOG = LoggerFactory.getLogger(PatientMergeServiceImpl.class);

    private final PatientRepository patientRepository;

    private final VisitRepository visitRepository;

    private final AppointmentRepository appointmentRepository;

    private final AuditLogService auditLogService;

    public PatientMergeServiceImpl(
        PatientRepository patientRepository,
        VisitRepository visitRepository,
        AppointmentRepository appointmentRepository,
        AuditLogService auditLogService
    ) {
        this.patientRepository = patientRepository;
        this.visitRepository = visitRepository;
        this.appointmentRepository = appointmentRepository;
        this.auditLogService = auditLogService;
    }

    @Override
    public PatientMergeResultDTO merge(MergePatientRequestDTO request) {
        if (request.getReason() == null || request.getReason().isBlank()) {
            // Checked here as well as on the DTO: bean validation only runs over HTTP, and a merge with no
            // stated justification is the one audit entry that must never be empty.
            throw BusinessRuleViolationException.of("mergeReasonRequired", "patient", "Merging patient records requires a reason");
        }

        Patient source = loadPatient(request.getSourcePatientId());
        Patient target = loadPatient(request.getTargetPatientId());

        if (source.getId().equals(target.getId())) {
            throw BusinessRuleViolationException.of(
                "mergeIntoItself",
                "patient",
                "Patient " + source.getId() + " cannot be merged into itself"
            );
        }
        if (PatientMerge.isAlreadyMerged(source.getRegistrationStatus())) {
            throw BusinessRuleViolationException.of(
                "sourceAlreadyMerged",
                "patient",
                "Temporary record " +
                source.getHospitalId() +
                " has already been merged into " +
                (source.getMergedIntoPatient() == null ? "another record" : source.getMergedIntoPatient().getHospitalId())
            );
        }
        if (PatientMerge.isAlreadyMerged(target.getRegistrationStatus())) {
            // Merging onto a record that is itself a pointer would create a chain nobody can follow, and
            // would put clinical history on a record that is not a patient.
            throw BusinessRuleViolationException.of(
                "targetAlreadyMerged",
                "patient",
                "Patient " +
                target.getHospitalId() +
                " has itself been merged into another record, so it cannot absorb anything"
            );
        }
        if (!PatientMerge.isMergeable(source.getRegistrationStatus(), target.getRegistrationStatus())) {
            throw BusinessRuleViolationException.of(
                "notMergeable",
                "patient",
                "Merging requires a temporary record adopting a confirmed one, but this is " +
                source.getRegistrationStatus() +
                " into " +
                target.getRegistrationStatus() +
                ". Two confirmed records both have their own identity, so which to keep is a decision this " +
                "action cannot make"
            );
        }

        // Everything clinical hangs off the visit — vitals, consultation, orders and their results,
        // prescriptions, referrals, the bill — and the visit is the only thing pointing at the patient. So
        // moving the visits moves all of it, and there is no per-entity list to keep in step. Only
        // appointments also name a patient directly.
        List<Visit> visits = visitRepository.findByPatientId(source.getId());
        List<Appointment> appointments = appointmentRepository.findByPatientId(source.getId());

        // Reassigned one at a time rather than with a bulk update: these rows carry a version, and a bulk
        // update would step around optimistic locking on records another station might be holding.
        for (Visit visit : visits) {
            visit.setPatient(target);
            visitRepository.save(visit);
        }
        for (Appointment appointment : appointments) {
            appointment.setPatient(target);
            appointmentRepository.save(appointment);
        }

        String temporaryHospitalId = source.getHospitalId();
        source.setRegistrationStatus(RegistrationStatus.MERGED);
        source.setMergedIntoPatient(target);
        source = patientRepository.save(source);

        int openVisits = (int) visits.stream().filter(visit -> VisitLifecycle.isOpen(visit.getStatus())).count();

        // Recorded on both records. Anyone reading the temporary record's history has to see where its
        // patient went, and anyone reading the real record's history has to see what it absorbed.
        auditLogService.record(
            AuditLogService.Entry.of(AuditActions.PATIENT_MERGED, "Patient", source.getId())
                .withReason(request.getReason())
                .withChange(temporaryHospitalId, target.getHospitalId())
                .withDetails(
                    "%d visit(s) and %d appointment(s) moved to %s".formatted(visits.size(), appointments.size(), target.getHospitalId())
                )
        );
        auditLogService.record(
            AuditLogService.Entry.of(AuditActions.PATIENT_MERGED, "Patient", target.getId())
                .withReason(request.getReason())
                .withChange(null, temporaryHospitalId)
                .withDetails(
                    "Absorbed record %s: %d visit(s), %d appointment(s)".formatted(
                            temporaryHospitalId,
                            visits.size(),
                            appointments.size()
                        )
                )
        );

        LOG.info(
            "Merged patient {} into {}: {} visit(s), {} appointment(s), {} still open",
            temporaryHospitalId,
            target.getHospitalId(),
            visits.size(),
            appointments.size(),
            openVisits
        );

        return new PatientMergeResultDTO(
            source.getId(),
            temporaryHospitalId,
            source.getRegistrationStatus(),
            target.getId(),
            target.getHospitalId(),
            visits.size(),
            appointments.size(),
            openVisits,
            request.getReason(),
            Instant.now()
        );
    }

    private Patient loadPatient(Long patientId) {
        return patientRepository
            .findById(patientId)
            .orElseThrow(() -> BusinessRuleViolationException.of("patientNotFound", "patient", "No patient with id " + patientId));
    }
}
