package com.hyperbrains.hms.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hyperbrains.hms.IntegrationTest;
import com.hyperbrains.hms.domain.Patient;
import com.hyperbrains.hms.domain.enumeration.IdentityDocumentType;
import com.hyperbrains.hms.domain.enumeration.RegistrationStatus;
import com.hyperbrains.hms.domain.enumeration.Sex;
import com.hyperbrains.hms.repository.PatientRepository;
import com.hyperbrains.hms.service.AuditLogService;
import com.hyperbrains.hms.service.BusinessRuleViolationException;
import com.hyperbrains.hms.service.ExactPatientMatchException;
import com.hyperbrains.hms.service.HospitalIdService;
import com.hyperbrains.hms.service.PatientCorrectionService;
import com.hyperbrains.hms.service.dto.view.CorrectPatientRequestDTO;
import com.hyperbrains.hms.service.dto.view.PatientCorrectionResultDTO;
import com.hyperbrains.hms.service.dto.view.RecordHistoryEntryDTO;
import com.hyperbrains.hms.service.impl.PatientCorrectionServiceImpl;
import com.hyperbrains.hms.service.rules.PatientFields;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;

/**
 * Integration tests for correcting a patient record.
 *
 * <p>This is one of the two correction patterns the specification separates. A patient's details
 * <em>are</em> edited in place — the stored value is the single source of truth — but never without a
 * reason, and never without a record of what the value used to be. The other pattern, the consultation
 * addendum, is tested in {@code ConsultationIT}; mixing the two up is the mistake these tests exist to
 * prevent.
 */
@IntegrationTest
@WithMockUser(value = "admin", authorities = "ROLE_RECEPTION")
class PatientCorrectionIT {

    @Autowired
    private PatientCorrectionService correctionService;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private HospitalIdService hospitalIdService;

    private Patient patient;

    /** Every record this test created, so teardown removes exactly those and nothing else. */
    private final List<Long> createdPatientIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        patient = patient("Mistyped Patient", "0700000000", null);
    }

    @AfterEach
    void cleanup() {
        createdPatientIds.forEach(id -> patientRepository.findById(id).ifPresent(patientRepository::delete));
        createdPatientIds.clear();
    }

    // ---------------------------------------------------------------- edit in place, with a reason

    @Test
    void aCorrectionChangesTheStoredValueAndKeepsWhatWasThereBefore() {
        CorrectPatientRequestDTO request = correction("Registering clerk transposed two digits");
        request.setPhone("0711111111");

        PatientCorrectionResultDTO result = correctionService.correct(patient.getId(), request);

        assertThat(result.changedFields()).containsExactly("phone");
        assertThat(patientRepository.findById(patient.getId()).orElseThrow().getPhone()).isEqualTo("0711111111");

        RecordHistoryEntryDTO entry = trail().getLast();
        assertThat(entry.fieldName()).isEqualTo("phone");
        assertThat(entry.previousValue()).isEqualTo("0700000000");
        assertThat(entry.currentValue()).isEqualTo("0711111111");
        assertThat(entry.reason()).isEqualTo("Registering clerk transposed two digits");
        assertThat(entry.actorLogin()).isEqualTo("admin");
    }

    /** Several fields in one request means one history row each, so the change can be read field by field. */
    @Test
    void eachChangedFieldGetsItsOwnHistoryEntry() {
        CorrectPatientRequestDTO request = correction("Patient gave the correct details at the desk");
        request.setPhone("0722222222");
        request.setNextOfKinName("Corrected Next Of Kin");
        request.setFullName("Corrected Patient");

        PatientCorrectionResultDTO result = correctionService.correct(patient.getId(), request);

        assertThat(result.changedFields()).containsExactlyInAnyOrder("phone", "nextOfKinName", "fullName");
        assertThat(trail())
            .filteredOn(entry -> "PATIENT_CORRECTED".equals(entry.action()))
            .extracting(RecordHistoryEntryDTO::fieldName)
            .containsExactlyInAnyOrder("phone", "nextOfKinName", "fullName");
    }

    /**
     * A correction that changes nothing must record nothing. Otherwise a caller re-sending the values it
     * already held would fill the history with rows saying a field changed to what it already was.
     */
    @Test
    void aCorrectionThatChangesNothingRecordsNothing() {
        CorrectPatientRequestDTO request = correction("Nothing actually to fix");
        request.setPhone("0700000000");

        PatientCorrectionResultDTO result = correctionService.correct(patient.getId(), request);

        assertThat(result.changedFields()).isEmpty();
        assertThat(trail()).isEmpty();
    }

    @Test
    void clearingAFieldIsACorrectionLikeAnyOther() {
        CorrectPatientRequestDTO request = correction("Phone number belonged to another patient");
        request.setPhone("");

        PatientCorrectionResultDTO result = correctionService.correct(patient.getId(), request);

        assertThat(result.changedFields()).containsExactly("phone");
        assertThat(patientRepository.findById(patient.getId()).orElseThrow().getPhone()).isEmpty();
    }

    @Test
    void aCorrectionWithoutAReasonIsRefused() {
        CorrectPatientRequestDTO request = correction("   ");
        request.setPhone("0733333333");

        assertThatThrownBy(() -> correctionService.correct(patient.getId(), request))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("requires a reason");

        // And nothing moved.
        assertThat(patientRepository.findById(patient.getId()).orElseThrow().getPhone()).isEqualTo("0700000000");
    }

    @Test
    void correctingAnUnknownPatientIsRefused() {
        CorrectPatientRequestDTO request = correction("No such patient");
        request.setPhone("0744444444");

        assertThatThrownBy(() -> correctionService.correct(999_999_999L, request))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("No patient with id");
    }

    // ---------------------------------------------------------------- who may change what

    /**
     * A recorded allergy is a clinical fact. The registration desk owns the route, because it owns the
     * contact details on it, but it does not own this.
     */
    @Test
    void theDeskMayNotChangeARecordedAllergy() {
        CorrectPatientRequestDTO request = correction("Patient said they are allergic to penicillin");
        request.setKnownAllergies("Penicillin");

        assertThatThrownBy(() -> correctionService.correct(patient.getId(), request))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("requires a clinical role");

        assertThat(patientRepository.findById(patient.getId()).orElseThrow().getKnownAllergies()).isNull();
    }

    @Test
    @WithMockUser(value = "admin", authorities = "ROLE_NURSE")
    void aClinicalRoleMayChangeARecordedAllergy() {
        CorrectPatientRequestDTO request = correction("Patient reported a penicillin allergy on arrival");
        request.setKnownAllergies("Penicillin");

        PatientCorrectionResultDTO result = correctionService.correct(patient.getId(), request);

        assertThat(result.changedFields()).containsExactly("knownAllergies");
        assertThat(patientRepository.findById(patient.getId()).orElseThrow().getKnownAllergies()).isEqualTo("Penicillin");
    }

    @Test
    @WithMockUser(value = "admin", authorities = "ROLE_RECEPTION")
    void theDeskMayStillCorrectContactDetails() {
        CorrectPatientRequestDTO request = correction("Next of kin number was wrong");
        request.setNextOfKinPhone("0755555555");

        assertThat(correctionService.correct(patient.getId(), request).changedFields()).containsExactly("nextOfKinPhone");
    }

    // ---------------------------------------------------------------- the duplicate check cannot be dodged

    /**
     * Registration refuses to save a second patient against an identity document already in use. If a
     * correction could set that document number freely, the check would be decoration.
     */
    @Test
    void aCorrectionCannotTakeAnIdentityDocumentThatIsAlreadyInUse() {
        patient("Document Holder One", "0700000001", "12345678");

        CorrectPatientRequestDTO request = correction("Patient produced their national ID");
        request.setIdentityDocumentType(IdentityDocumentType.NATIONAL_ID);
        request.setIdentityDocumentNumber("12345678");

        assertThatThrownBy(() -> correctionService.correct(patient.getId(), request))
            .isInstanceOf(ExactPatientMatchException.class)
            .hasMessageContaining("already in use");

        assertThat(patientRepository.findById(patient.getId()).orElseThrow().getIdentityDocumentNumber()).isNull();
    }

    /** The duplicate is offered rather than blocked, exactly as at registration: confirming it proceeds. */
    @Test
    void confirmingTheKnownDocumentLetsTheCorrectionThrough() {
        patient("Document Holder Two", "0700000002", "87654321");

        CorrectPatientRequestDTO request = correction("Confirmed with the patient and the existing record");
        request.setIdentityDocumentType(IdentityDocumentType.NATIONAL_ID);
        request.setIdentityDocumentNumber("87654321");
        request.setConfirmExistingIdentityDocument(true);

        assertThat(correctionService.correct(patient.getId(), request).changedFields()).containsExactly(
            "identityDocumentType",
            "identityDocumentNumber"
        );
    }

    /** Holding the same number as itself is not a duplicate. */
    @Test
    void aPatientKeepingTheirOwnIdentityDocumentIsNotADuplicate() {
        Patient holder = patient("Document Holder Three", "0700000003", "11112222");

        CorrectPatientRequestDTO request = correction("Spelling of the name was wrong");
        request.setIdentityDocumentType(IdentityDocumentType.NATIONAL_ID);
        request.setIdentityDocumentNumber("11112222");
        request.setFullName("Corrected Holder");

        assertThat(correctionService.correct(holder.getId(), request).changedFields()).containsExactly("fullName");
    }

    // ---------------------------------------------------------------- structural guards

    /**
     * The permanent identifier must never be a field somebody can type over: other records, letters and
     * index entries already refer to this patient by it. Asserted on the request type so that adding it
     * later fails here rather than in production.
     */
    @Test
    void thePermanentIdentifierIsNotEvenOfferedAsACorrectableField() {
        assertThat(fieldNames(CorrectPatientRequestDTO.class)).doesNotContain("hospitalId", "registrationStatus", "mergedIntoPatient");
    }

    /**
     * Every field a caller may correct has to appear in the snapshot, or it would be corrected without
     * being recorded. This ties the classification tables to the diff that actually runs.
     */
    @Test
    void everyCorrectableFieldIsCoveredByTheCorrectionSnapshot() {
        Set<String> snapshotted = PatientCorrectionServiceImpl.snapshot(patient).keySet();

        assertThat(snapshotted).containsAll(PatientFields.DEMOGRAPHIC);
        assertThat(snapshotted).containsAll(PatientFields.CLINICAL);
        assertThat(snapshotted).doesNotContainAnyElementsOf(PatientFields.NEVER_CORRECTABLE);
    }

    // ---------------------------------------------------------------- helpers

    private List<RecordHistoryEntryDTO> trail() {
        return auditLogService.trail("Patient", String.valueOf(patient.getId()));
    }

    private Patient patient(String fullName, String phone, String identityDocumentNumber) {
        Patient record = new Patient();
        record.setHospitalId(hospitalIdService.nextPermanentId());
        record.setFullName(fullName);
        record.setSex(Sex.FEMALE);
        record.setSexEstimated(false);
        record.setDateOfBirth(LocalDate.of(1991, 3, 4));
        record.setRegistrationStatus(RegistrationStatus.COMPLETE);
        record.setPhone(phone);
        record.setIdentityDocumentNumber(identityDocumentNumber);
        if (identityDocumentNumber != null) {
            // A document number without a type is not an identity anything can be matched on: registration
            // matches on the pair, and so does the correction guard.
            record.setIdentityDocumentType(IdentityDocumentType.NATIONAL_ID);
        }
        Patient saved = patientRepository.save(record);
        createdPatientIds.add(saved.getId());
        return saved;
    }

    private static CorrectPatientRequestDTO correction(String reason) {
        CorrectPatientRequestDTO request = new CorrectPatientRequestDTO();
        request.setReason(reason);
        return request;
    }

    private static List<String> fieldNames(Class<?> type) {
        return Stream.concat(Arrays.stream(type.getDeclaredFields()), Arrays.stream(type.getSuperclass().getDeclaredFields()))
            .map(Field::getName)
            .collect(Collectors.toList());
    }
}
