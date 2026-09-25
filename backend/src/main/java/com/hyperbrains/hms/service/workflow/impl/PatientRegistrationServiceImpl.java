package com.hyperbrains.hms.service.workflow.impl;

import com.hyperbrains.hms.config.HmsProperties;
import com.hyperbrains.hms.domain.Patient;
import com.hyperbrains.hms.domain.enumeration.RegistrationStatus;
import com.hyperbrains.hms.repository.PatientRepository;
import com.hyperbrains.hms.service.AuditActions;
import com.hyperbrains.hms.service.AuditLogService;
import com.hyperbrains.hms.service.ExactPatientMatchException;
import com.hyperbrains.hms.service.HospitalIdService;
import com.hyperbrains.hms.service.dto.view.DuplicateCheckResultDTO;
import com.hyperbrains.hms.service.dto.view.EmergencyIntakeRequestDTO;
import com.hyperbrains.hms.service.dto.view.PatientRegistrationRequestDTO;
import com.hyperbrains.hms.service.dto.view.PatientRegistrationResultDTO;
import com.hyperbrains.hms.service.dto.view.PatientSummaryDTO;
import com.hyperbrains.hms.service.dto.view.PossibleDuplicateDTO;
import com.hyperbrains.hms.service.mapper.PatientMapper;
import com.hyperbrains.hms.service.rules.PatientDuplicateMatcher;
import com.hyperbrains.hms.service.workflow.PatientRegistrationService;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registration workflow. The matching rules themselves live in
 * {@link PatientDuplicateMatcher}; this class is only responsible for gathering the candidates,
 * applying the outcome, and writing the audit trail.
 */
@Service
@Transactional
public class PatientRegistrationServiceImpl implements PatientRegistrationService {

    private static final Logger LOG = LoggerFactory.getLogger(PatientRegistrationServiceImpl.class);

    /** A patient nobody could name. The generated identifier is what distinguishes them. */
    private static final String UNIDENTIFIED_NAME = "Unknown";

    private final PatientRepository patientRepository;

    private final PatientMapper patientMapper;

    private final HospitalIdService hospitalIdService;

    private final AuditLogService auditLogService;

    private final HmsProperties properties;

    public PatientRegistrationServiceImpl(
        PatientRepository patientRepository,
        PatientMapper patientMapper,
        HospitalIdService hospitalIdService,
        AuditLogService auditLogService,
        HmsProperties properties
    ) {
        this.patientRepository = patientRepository;
        this.patientMapper = patientMapper;
        this.hospitalIdService = hospitalIdService;
        this.auditLogService = auditLogService;
        this.properties = properties;
    }

    @Override
    @Transactional(readOnly = true)
    public DuplicateCheckResultDTO checkForDuplicates(PatientRegistrationRequestDTO request) {
        return evaluate(toCandidate(request), candidatesFor(request));
    }

    @Override
    public PatientRegistrationResultDTO register(PatientRegistrationRequestDTO request) {
        List<Patient> existing = candidatesFor(request);
        DuplicateCheckResultDTO check = evaluate(toCandidate(request), existing);

        if (check.getExactMatch() != null && isBlank(request.getOverrideReason())) {
            // Hand back the record we matched so Reception can choose "use this one" instead.
            throw new ExactPatientMatchException(check.getExactMatch().getId(), check.getExactMatch().getHospitalId());
        }

        Patient patient = new Patient();
        applyRequest(patient, request);
        patient.setHospitalId(hospitalIdService.nextPermanentId());
        patient.setRegistrationStatus(RegistrationStatus.COMPLETE);
        patient = patientRepository.save(patient);

        auditLogService
            .record(AuditLogService.Entry.of(AuditActions.PATIENT_REGISTERED, "Patient", patient.getId()).withDetails(
                describeDuplicates(check.getPossibleDuplicates())
            ));

        if (check.getExactMatch() != null) {
            auditLogService.record(
                AuditLogService.Entry.of(AuditActions.PATIENT_DUPLICATE_OVERRIDE, "Patient", patient.getId())
                    .withReason(request.getOverrideReason())
                    .withDetails("Registration proceeded despite an exact match with " + check.getExactMatch().getHospitalId())
            );
            LOG.warn(
                "Patient {} registered over an exact identity-document match with {}",
                patient.getHospitalId(),
                check.getExactMatch().getHospitalId()
            );
        }

        return result(patient, check.getPossibleDuplicates());
    }

    @Override
    public PatientRegistrationResultDTO emergencyIntake(EmergencyIntakeRequestDTO request) {
        Patient patient = new Patient();
        patient.setFullName(isBlank(request.getFullName()) ? UNIDENTIFIED_NAME : request.getFullName());
        patient.setSex(request.getSex());
        patient.setSexEstimated(request.getSexEstimated());
        patient.setEstimatedAge(request.getEstimatedAge());
        patient.setHospitalId(hospitalIdService.nextTemporaryId());
        patient.setRegistrationStatus(RegistrationStatus.INCOMPLETE_REGISTRATION);
        patient = patientRepository.save(patient);

        auditLogService.record(
            AuditLogService.Entry.of(AuditActions.PATIENT_EMERGENCY_INTAKE, "Patient", patient.getId())
                .withReason(request.getIntakeNotes())
                .withDetails("Temporary identifier issued; record must be merged into a confirmed one")
        );

        // No duplicate check: this path exists precisely because there is no identity to match on,
        // and blocking it would delay treatment.
        return result(patient, List.of());
    }

    private PatientRegistrationResultDTO result(Patient patient, List<PossibleDuplicateDTO> duplicates) {
        PatientRegistrationResultDTO dto = new PatientRegistrationResultDTO();
        dto.setPatient(patientMapper.toDto(patient));
        dto.setPossibleDuplicates(duplicates);
        return dto;
    }

    /**
     * Gathers the candidates the matcher scores.
     *
     * <p>Two queries on purpose. The name/phone/date pre-filter keeps the common case cheap, but it
     * keys off the name — and someone re-registering a patient under a completely different name
     * (a misspelling, a married name, a transliteration) would slip past it. The identity-document
     * query closes that gap, which matters because that check is the one that blocks a save.
     */
    private List<Patient> candidatesFor(PatientRegistrationRequestDTO request) {
        Map<Long, Patient> byId = new LinkedHashMap<>();
        String nameToken = PatientDuplicateMatcher.leadingNameToken(request.getFullName());
        String phone = orEmpty(request.getPhone());
        LocalDate dateOfBirth = request.getDateOfBirth();

        patientRepository
            .findDuplicateCandidates(
                !nameToken.isEmpty(),
                nameToken,
                !phone.isEmpty(),
                phone,
                dateOfBirth != null,
                dateOfBirth,
                RegistrationStatus.MERGED
            )
            .forEach(patient -> byId.put(patient.getId(), patient));

        normalizedDocument(request).ifPresent(normalized ->
            patientRepository
                .findByIdentityDocumentNormalized(request.getIdentityDocumentType().name(), normalized)
                .forEach(patient -> byId.put(patient.getId(), patient))
        );

        return List.copyOf(byId.values());
    }

    private DuplicateCheckResultDTO evaluate(PatientDuplicateMatcher.Candidate candidate, List<Patient> existingPatients) {
        Map<Long, Patient> byId = existingPatients.stream().collect(Collectors.toMap(Patient::getId, patient -> patient));
        List<PatientDuplicateMatcher.ExistingPatient> existing = existingPatients.stream().map(this::toExisting).toList();

        DuplicateCheckResultDTO result = new DuplicateCheckResultDTO();
        result.setExactMatch(
            PatientDuplicateMatcher.exactIdentityDocumentMatch(candidate, existing)
                .map(match -> PatientSummaryDTO.from(byId.get(match.patient().id())))
                .orElse(null)
        );
        result.setPossibleDuplicates(
            PatientDuplicateMatcher
                .possibleDuplicates(candidate, existing, settings(), LocalDate.now(ZoneOffset.UTC))
                .stream()
                .map(PossibleDuplicateDTO::from)
                .toList()
        );
        result.setOverrideRequired(result.getExactMatch() != null);
        return result;
    }

    private PatientDuplicateMatcher.Settings settings() {
        HmsProperties.Duplicate duplicate = properties.getDuplicate();
        return new PatientDuplicateMatcher.Settings(
            duplicate.getNameSimilarityThreshold(),
            duplicate.getStrongNameSimilarity(),
            duplicate.getAgeToleranceYears()
        );
    }

    private PatientDuplicateMatcher.Candidate toCandidate(PatientRegistrationRequestDTO request) {
        return new PatientDuplicateMatcher.Candidate(
            request.getFullName(),
            request.getDateOfBirth(),
            request.getEstimatedAge(),
            request.getPhone(),
            request.getIdentityDocumentType(),
            request.getIdentityDocumentNumber()
        );
    }

    private PatientDuplicateMatcher.ExistingPatient toExisting(Patient patient) {
        return new PatientDuplicateMatcher.ExistingPatient(
            patient.getId(),
            patient.getHospitalId(),
            patient.getFullName(),
            patient.getDateOfBirth(),
            patient.getEstimatedAge(),
            patient.getPhone(),
            patient.getIdentityDocumentType(),
            patient.getIdentityDocumentNumber()
        );
    }

    private Optional<String> normalizedDocument(PatientRegistrationRequestDTO request) {
        if (request.getIdentityDocumentType() == null) {
            return Optional.empty();
        }
        String normalized = PatientDuplicateMatcher.normalizeDocumentNumber(request.getIdentityDocumentNumber());
        return normalized.isEmpty() ? Optional.empty() : Optional.of(normalized);
    }

    private void applyRequest(Patient patient, PatientRegistrationRequestDTO request) {
        patient.setFullName(request.getFullName());
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setEstimatedAge(request.getEstimatedAge());
        patient.setSex(request.getSex());
        patient.setSexEstimated(request.getSexEstimated());
        patient.setPhone(request.getPhone());
        patient.setEmail(request.getEmail());
        patient.setIdentityDocumentType(request.getIdentityDocumentType());
        patient.setIdentityDocumentNumber(request.getIdentityDocumentNumber());
        patient.setOccupation(request.getOccupation());
        patient.setMaritalStatus(request.getMaritalStatus());
        patient.setNextOfKinName(request.getNextOfKinName());
        patient.setNextOfKinPhone(request.getNextOfKinPhone());
        patient.setNextOfKinRelationship(request.getNextOfKinRelationship());
        patient.setKnownAllergies(request.getKnownAllergies());
        patient.setKnownConditions(request.getKnownConditions());
        patient.setVillageEstate(request.getVillageEstate());
    }

    /** Recorded on the audit entry so a later reviewer can see what was shown, and ignored. */
    private static String describeDuplicates(List<PossibleDuplicateDTO> duplicates) {
        if (duplicates == null || duplicates.isEmpty()) {
            return null;
        }
        return duplicates
            .stream()
            .map(duplicate ->
                duplicate.getHospitalId() + " [similarity " + duplicate.getNameSimilarity() + ", " + String.join("+", duplicate.getReasons()) + "]"
            )
            .collect(Collectors.joining("; "));
    }

    private static String orEmpty(String value) {
        return value == null ? "" : value;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
