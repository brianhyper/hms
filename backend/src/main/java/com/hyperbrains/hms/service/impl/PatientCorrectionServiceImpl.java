package com.hyperbrains.hms.service.impl;

import com.hyperbrains.hms.domain.Patient;
import com.hyperbrains.hms.repository.PatientRepository;
import com.hyperbrains.hms.security.AuthoritiesConstants;
import com.hyperbrains.hms.security.SecurityUtils;
import com.hyperbrains.hms.service.AuditActions;
import com.hyperbrains.hms.service.AuditLogService;
import com.hyperbrains.hms.service.BusinessRuleViolationException;
import com.hyperbrains.hms.service.ExactPatientMatchException;
import com.hyperbrains.hms.service.PatientCorrectionService;
import com.hyperbrains.hms.service.dto.view.CorrectPatientRequestDTO;
import com.hyperbrains.hms.service.dto.view.PatientCorrectionResultDTO;
import com.hyperbrains.hms.service.rules.PatientDuplicateMatcher;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PatientCorrectionServiceImpl implements PatientCorrectionService {

    private static final Logger LOG = LoggerFactory.getLogger(PatientCorrectionServiceImpl.class);

    private final PatientRepository patientRepository;

    private final AuditLogService auditLogService;

    public PatientCorrectionServiceImpl(PatientRepository patientRepository, AuditLogService auditLogService) {
        this.patientRepository = patientRepository;
        this.auditLogService = auditLogService;
    }

    @Override
    public PatientCorrectionResultDTO correct(Long patientId, CorrectPatientRequestDTO request) {
        // Re-checked here as well as on the DTO: bean validation only runs when the request comes in over
        // HTTP, and a correction nobody can explain is exactly what this endpoint exists to prevent.
        if (request.getReason() == null || request.getReason().isBlank()) {
            throw BusinessRuleViolationException.of(
                "correctionReasonRequired",
                "patient",
                "Correcting a patient record requires a reason"
            );
        }

        Patient patient = loadPatient(patientId);

        requireClinicalAuthorityFor(request);
        requireIdentityDocumentAvailable(patientId, request);

        // Snapshotted before and after rather than derived from the request: the request says what was
        // asked for, the snapshot says what is now stored, and the trail must describe the second.
        Map<String, String> before = snapshot(patient);
        apply(request, patient);
        patient = patientRepository.save(patient);
        Map<String, String> after = snapshot(patient);

        List<AuditLogService.FieldChange> changes = auditLogService.recordCorrection(
            AuditActions.PATIENT_CORRECTED,
            "Patient",
            patient.getId(),
            request.getReason(),
            before,
            after
        );

        LOG.debug("Corrected patient {}: {} field(s) changed", patientId, changes.size());
        return new PatientCorrectionResultDTO(
            patient.getId(),
            request.getReason(),
            changes.stream().map(AuditLogService.FieldChange::field).toList(),
            Instant.now()
        );
    }

    /**
     * A recorded allergy is a clinical fact.
     *
     * <p>Checked against what was <em>supplied</em> rather than what changed, so a caller cannot probe or
     * quietly confirm clinical values from the registration desk. Roles are decided here rather than in the
     * authorization table because this is finer than an endpoint: the same route is used by the desk for a
     * phone number and by a clinician for an allergy.
     */
    private static void requireClinicalAuthorityFor(CorrectPatientRequestDTO request) {
        boolean touchesClinical = request.getKnownAllergies() != null || request.getKnownConditions() != null;
        if (
            touchesClinical &&
            !SecurityUtils.hasCurrentUserAnyOfAuthorities(
                AuthoritiesConstants.NURSE,
                AuthoritiesConstants.DOCTOR,
                AuthoritiesConstants.ADMIN,
                AuthoritiesConstants.SUPER_ADMIN
            )
        ) {
            throw new AccessDeniedException(
                "Changing a recorded allergy or condition requires a clinical role"
            );
        }
    }

    /**
     * A correction must not be a way round the registration duplicate check.
     *
     * <p>Registration refuses to save a second patient against an identity document that is already in
     * use unless someone explicitly confirms it. If that check can be bypassed by correcting the document
     * number after the fact, the check is decoration.
     */
    private void requireIdentityDocumentAvailable(Long patientId, CorrectPatientRequestDTO request) {
        if (request.getIdentityDocumentNumber() == null || request.getIdentityDocumentType() == null) {
            // Without both, the document is not a complete identity and nothing can be matched on it.
            return;
        }

        String normalized = PatientDuplicateMatcher.normalizeDocumentNumber(request.getIdentityDocumentNumber());
        if (normalized.isEmpty() || Boolean.TRUE.equals(request.getConfirmExistingIdentityDocument())) {
            return;
        }

        List<Patient> holders = patientRepository
            .findByIdentityDocumentNormalized(request.getIdentityDocumentType().name(), normalized)
            .stream()
            .filter(existing -> !existing.getId().equals(patientId))
            .toList();

        if (!holders.isEmpty()) {
            Patient holder = holders.getFirst();
            throw new ExactPatientMatchException(holder.getId(), holder.getHospitalId());
        }
    }

    /**
     * The correctable fields, by name, as text.
     *
     * <p>Every field a caller may correct has to appear here or it would be corrected without being
     * recorded, so the set of keys is asserted against the classification rules in the tests.
     */
    public static Map<String, String> snapshot(Patient patient) {        Map<String, String> values = new LinkedHashMap<>();
        values.put("fullName", patient.getFullName());
        values.put("dateOfBirth", text(patient.getDateOfBirth()));
        values.put("estimatedAge", text(patient.getEstimatedAge()));
        values.put("sex", text(patient.getSex()));
        values.put("sexEstimated", text(patient.getSexEstimated()));
        values.put("phone", patient.getPhone());
        values.put("email", patient.getEmail());
        values.put("identityDocumentType", text(patient.getIdentityDocumentType()));
        values.put("identityDocumentNumber", patient.getIdentityDocumentNumber());
        values.put("occupation", patient.getOccupation());
        values.put("maritalStatus", patient.getMaritalStatus());
        values.put("nextOfKinName", patient.getNextOfKinName());
        values.put("nextOfKinPhone", patient.getNextOfKinPhone());
        values.put("nextOfKinRelationship", text(patient.getNextOfKinRelationship()));
        values.put("villageEstate", patient.getVillageEstate());
        values.put("knownAllergies", patient.getKnownAllergies());
        values.put("knownConditions", patient.getKnownConditions());
        return values;
    }

    /** Only what was supplied is applied, which is what makes correcting one field a one-field request. */
    private static void apply(CorrectPatientRequestDTO request, Patient patient) {
        if (request.getFullName() != null) {
            patient.setFullName(request.getFullName());
        }
        if (request.getDateOfBirth() != null) {
            patient.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getEstimatedAge() != null) {
            patient.setEstimatedAge(request.getEstimatedAge());
        }
        if (request.getSex() != null) {
            patient.setSex(request.getSex());
        }
        if (request.getSexEstimated() != null) {
            patient.setSexEstimated(request.getSexEstimated());
        }
        if (request.getPhone() != null) {
            patient.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            patient.setEmail(request.getEmail());
        }
        if (request.getIdentityDocumentType() != null) {
            patient.setIdentityDocumentType(request.getIdentityDocumentType());
        }
        if (request.getIdentityDocumentNumber() != null) {
            patient.setIdentityDocumentNumber(request.getIdentityDocumentNumber());
        }
        if (request.getOccupation() != null) {
            patient.setOccupation(request.getOccupation());
        }
        if (request.getMaritalStatus() != null) {
            patient.setMaritalStatus(request.getMaritalStatus());
        }
        if (request.getNextOfKinName() != null) {
            patient.setNextOfKinName(request.getNextOfKinName());
        }
        if (request.getNextOfKinPhone() != null) {
            patient.setNextOfKinPhone(request.getNextOfKinPhone());
        }
        if (request.getNextOfKinRelationship() != null) {
            patient.setNextOfKinRelationship(request.getNextOfKinRelationship());
        }
        if (request.getVillageEstate() != null) {
            patient.setVillageEstate(request.getVillageEstate());
        }
        if (request.getKnownAllergies() != null) {
            patient.setKnownAllergies(request.getKnownAllergies());
        }
        if (request.getKnownConditions() != null) {
            patient.setKnownConditions(request.getKnownConditions());
        }
    }

    private static String text(Object value) {
        return value == null ? null : value.toString();
    }

    private Patient loadPatient(Long patientId) {
        return patientRepository
            .findById(patientId)
            .orElseThrow(() -> BusinessRuleViolationException.of("patientNotFound", "patient", "No patient with id " + patientId));
    }
}
