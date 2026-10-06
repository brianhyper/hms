package com.hyperbrains.hms.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hyperbrains.hms.IntegrationTest;
import com.hyperbrains.hms.domain.Patient;
import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.domain.enumeration.IdentityDocumentType;
import com.hyperbrains.hms.domain.enumeration.RegistrationStatus;
import com.hyperbrains.hms.domain.enumeration.Sex;
import com.hyperbrains.hms.repository.AuditLogRepository;
import com.hyperbrains.hms.repository.PatientRepository;
import com.hyperbrains.hms.repository.UserRepository;
import com.hyperbrains.hms.service.AuditActions;
import com.hyperbrains.hms.service.BusinessRuleViolationException;
import com.hyperbrains.hms.service.ExactPatientMatchException;
import com.hyperbrains.hms.service.HospitalIdService;
import com.hyperbrains.hms.service.dto.view.DuplicateCheckResultDTO;
import com.hyperbrains.hms.service.dto.view.EmergencyIntakeRequestDTO;
import com.hyperbrains.hms.service.dto.view.PatientRegistrationRequestDTO;
import com.hyperbrains.hms.service.dto.view.PatientRegistrationResultDTO;
import com.hyperbrains.hms.service.workflow.PatientRegistrationService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

/**
 * Integration tests for registration.
 *
 * <p>Driven through the service rather than MockMvc because the interesting behaviour is
 * transactional and database-bound: the identifier comes from a Postgres sequence, the exact-match
 * check uses a native normalising query, and the audit trail is written on the way through. A
 * mocked web-layer test would not exercise any of it.
 */
@IntegrationTest
@WithMockUser(value = "admin", authorities = "ROLE_RECEPTION")
class PatientRegistrationIT {

    @Autowired
    private PatientRegistrationService patientRegistrationService;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private HospitalIdService hospitalIdService;

    @Autowired
    private UserRepository userRepository;

    /** The account the mocked authentication resolves to, used to check audit attribution. */
    private User adminUser;

    private final List<Long> createdPatientIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        adminUser = userRepository.findOneByLogin("admin").orElseThrow();
    }

    @AfterEach
    void cleanup() {
        // deleteById rather than delete(entity): Patient carries a version column only indirectly
        // via other entities, but keeping the pattern consistent avoids stale-entity surprises.
        createdPatientIds.forEach(id -> patientRepository.findById(id).ifPresent(patientRepository::delete));
        createdPatientIds.clear();
    }

    @Test
    void assignsAPermanentIdentifierAndCompletesRegistration() {
        PatientRegistrationResultDTO result = patientRegistrationService.register(request("Amina Yusuf", null, null));

        remember(result);
        assertThat(result.getPatient().getHospitalId()).startsWith("HMS-");
        assertThat(result.getPatient().getRegistrationStatus()).isEqualTo(RegistrationStatus.COMPLETE);
        assertThat(result.getPossibleDuplicates()).isEmpty();
    }

    /** Two registrations in a row must not be handed the same identifier. */
    @Test
    void issuesDistinctIdentifiersToConsecutiveRegistrations() {
        PatientRegistrationResultDTO first = patientRegistrationService.register(request("Bahati Omondi", null, null));
        remember(first);
        PatientRegistrationResultDTO second = patientRegistrationService.register(request("Chausiku Mwangi", null, null));
        remember(second);

        assertThat(first.getPatient().getHospitalId()).isNotEqualTo(second.getPatient().getHospitalId());
    }

    @Test
    void refusesToRegisterASecondPatientOnTheSameIdentityDocument() {
        PatientRegistrationResultDTO existing = patientRegistrationService.register(
            request("Daniel Kiptoo", IdentityDocumentType.NATIONAL_ID, "999-888-77")
        );
        remember(existing);

        // Same document, different formatting: the check must normalise before comparing.
        PatientRegistrationRequestDTO duplicate = request("Daniel Kiptoo", IdentityDocumentType.NATIONAL_ID, "99988877");

        assertThatThrownBy(() -> patientRegistrationService.register(duplicate))
            .isInstanceOf(ExactPatientMatchException.class)
            .satisfies(thrown -> {
                ExactPatientMatchException match = (ExactPatientMatchException) thrown;
                assertThat(match.getExistingHospitalId()).isEqualTo(existing.getPatient().getHospitalId());
                assertThat(match.getExistingPatientId()).isEqualTo(existing.getPatient().getId());
            });
    }

    @Test
    void allowsTheSecondRecordWhenReceptionExplicitlyOverrides() {
        PatientRegistrationResultDTO existing = patientRegistrationService.register(
            request("Esther Njeri", IdentityDocumentType.NATIONAL_ID, "11122233")
        );
        remember(existing);

        PatientRegistrationRequestDTO override = request("Esther Njeri", IdentityDocumentType.NATIONAL_ID, "11122233");
        override.setOverrideReason("Verified as two different people sharing an ID number in error");

        PatientRegistrationResultDTO created = patientRegistrationService.register(override);
        remember(created);

        assertThat(created.getPatient().getId()).isNotEqualTo(existing.getPatient().getId());
        assertThat(created.getPatient().getHospitalId()).isNotEqualTo(existing.getPatient().getHospitalId());
    }

    @Test
    void recordsAnAuditEntryForTheOverride() {
        PatientRegistrationResultDTO existing = patientRegistrationService.register(
            request("Furaha Juma", IdentityDocumentType.NATIONAL_ID, "44455566")
        );
        remember(existing);

        PatientRegistrationRequestDTO override = request("Furaha Juma", IdentityDocumentType.NATIONAL_ID, "44455566");
        override.setOverrideReason("Confirmed distinct by next of kin");
        PatientRegistrationResultDTO created = patientRegistrationService.register(override);
        remember(created);

        assertThat(auditLogRepository.findAll())
            .filteredOn(entry -> AuditActions.PATIENT_DUPLICATE_OVERRIDE.equals(entry.getAction()))
            .filteredOn(entry -> created.getPatient().getId().toString().equals(entry.getEntityId()))
            .singleElement()
            .satisfies(entry -> {
                assertThat(entry.getReason()).isEqualTo("Confirmed distinct by next of kin");
                // Attributing the override to the actual caller is the point of the entry. Only the
                // identifier is read: the actor is a lazy proxy and this test runs outside a session,
                // so touching any other property would trigger a lazy initialisation failure.
                assertThat(entry.getActor()).isNotNull();
                assertThat(entry.getActor().getId()).isEqualTo(adminUser.getId());
            });
    }

    @Test
    void aMergedRecordDoesNotBlockRegistration() {
        Patient merged = new Patient();
        merged.setHospitalId(hospitalIdService.nextPermanentId());
        merged.setFullName("Gwendo Merged");
        merged.setSex(Sex.FEMALE);
        merged.setSexEstimated(false);
        merged.setRegistrationStatus(RegistrationStatus.MERGED);
        merged.setIdentityDocumentType(IdentityDocumentType.NATIONAL_ID);
        merged.setIdentityDocumentNumber("77788899");
        merged = patientRepository.save(merged);
        createdPatientIds.add(merged.getId());

        // A merged record has been superseded, so it must not stand in the way of a new patient.
        PatientRegistrationResultDTO created = patientRegistrationService.register(
            request("Gwendo Real", IdentityDocumentType.NATIONAL_ID, "77788899")
        );
        remember(created);

        assertThat(created.getPatient().getRegistrationStatus()).isEqualTo(RegistrationStatus.COMPLETE);
    }

    @Test
    void emergencyIntakeIssuesATemporaryIdentifierAndLeavesRegistrationIncomplete() {
        EmergencyIntakeRequestDTO request = new EmergencyIntakeRequestDTO();
        request.setSex(Sex.MALE);
        request.setSexEstimated(true);
        request.setEstimatedAge(35);
        request.setIntakeNotes("Unconscious, brought in by bystanders, no documents");

        PatientRegistrationResultDTO result = patientRegistrationService.emergencyIntake(request);
        remember(result);

        assertThat(result.getPatient().getHospitalId()).startsWith("UNK-");
        assertThat(result.getPatient().getRegistrationStatus()).isEqualTo(RegistrationStatus.INCOMPLETE_REGISTRATION);
        // Treatment must not be blocked waiting for a registration desk to fill in a name.
        assertThat(result.getPatient().getFullName()).isNotBlank();
    }

    @Test
    void surfacesAPossibleDuplicateWhenNameAndDateOfBirthCorroborate() {
        // An adult with no document states that explicitly at the desk (see the identity rule); the point of this
        // test is the advisory match, not the identity.
        PatientRegistrationRequestDTO original = request("Hadija Wanjala", IdentityDocumentType.PENDING, null);
        original.setDateOfBirth(LocalDate.of(1988, 4, 12));
        original.setPhone("0700111222");
        PatientRegistrationResultDTO existing = patientRegistrationService.register(original);
        remember(existing);

        // One character apart, same date of birth: two independent signals.
        PatientRegistrationRequestDTO nearMiss = request("Hadija Wanjalaa", null, null);
        nearMiss.setDateOfBirth(LocalDate.of(1988, 4, 12));

        DuplicateCheckResultDTO check = patientRegistrationService.checkForDuplicates(nearMiss);

        assertThat(check.isOverrideRequired()).isFalse();
        assertThat(check.getPossibleDuplicates())
            .anySatisfy(duplicate -> {
                assertThat(duplicate.getPatientId()).isEqualTo(existing.getPatient().getId());
                assertThat(duplicate.getReasons()).contains("SIMILAR_NAME", "SAME_DATE_OF_BIRTH");
            });
    }

    /**
     * An adult must produce a document, or say explicitly that it is pending. Silence is refused because a file
     * with no identifier cannot be matched to a person when it matters: at admission, in a duplicate review, or
     * when a result has to be handed to the right person.
     */
    @Test
    void refusesAnAdultWhoHasNeitherADocumentNorAPendingMarker() {
        PatientRegistrationRequestDTO adult = request("Ibrahim Njoroge", null, null);
        adult.setDateOfBirth(LocalDate.of(1990, 3, 2));

        assertThatThrownBy(() -> patientRegistrationService.register(adult))
            .isInstanceOf(BusinessRuleViolationException.class)
            .satisfies(thrown -> assertThat(((BusinessRuleViolationException) thrown).getErrorKey()).isEqualTo("identityDocumentRequired"));
    }

    /** The explicit marker is the escape hatch: the desk states the identity is pending instead of staying silent. */
    @Test
    void acceptsAnAdultWhoseIdentityIsExplicitlyPending() {
        PatientRegistrationRequestDTO adult = request("Josephine Adhiambo", IdentityDocumentType.PENDING, null);
        adult.setDateOfBirth(LocalDate.of(1985, 7, 19));

        PatientRegistrationResultDTO result = patientRegistrationService.register(adult);
        remember(result);

        assertThat(result.getPatient().getIdentityDocumentType()).isEqualTo(IdentityDocumentType.PENDING);
        assertThat(result.getPatient().getIdentityDocumentNumber()).isNull();
    }

    /** The rule is about adults. A child is often without a document, and refusing one would refuse those who need registering most. */
    @Test
    void acceptsAChildWithNoDocumentAndNoMarker() {
        PatientRegistrationRequestDTO child = request("Mercy Wairimu", null, null);
        child.setDateOfBirth(LocalDate.of(2020, 11, 4));

        PatientRegistrationResultDTO result = patientRegistrationService.register(child);
        remember(result);

        assertThat(result.getPatient().getRegistrationStatus()).isEqualTo(RegistrationStatus.COMPLETE);
    }

    /** The marker says the number is not known yet; sending one with it is a contradiction, not a shortcut. */
    @Test
    void refusesAPendingMarkerThatCarriesANumber() {
        PatientRegistrationRequestDTO contradictory = request("Kevin Mburu", IdentityDocumentType.PENDING, "12345678");
        contradictory.setDateOfBirth(LocalDate.of(1992, 1, 30));

        assertThatThrownBy(() -> patientRegistrationService.register(contradictory))
            .isInstanceOf(BusinessRuleViolationException.class)
            .satisfies(thrown ->
                assertThat(((BusinessRuleViolationException) thrown).getErrorKey()).isEqualTo("identityDocumentContradiction")
            );
    }

    /** A named document type with no number cannot be matched on, so it is refused rather than stored half-known. */
    @Test
    void refusesANamedDocumentTypeWithNoNumber() {
        PatientRegistrationRequestDTO nameless = request("Lydia Chebet", IdentityDocumentType.NATIONAL_ID, null);
        nameless.setDateOfBirth(LocalDate.of(1991, 5, 6));

        assertThatThrownBy(() -> patientRegistrationService.register(nameless))
            .isInstanceOf(BusinessRuleViolationException.class)
            .satisfies(thrown ->
                assertThat(((BusinessRuleViolationException) thrown).getErrorKey()).isEqualTo("identityDocumentContradiction")
            );
    }

    /** The worklist names the patient, when they were registered and who did it, so the desk can chase the document. */
    @Test
    void listsAdultsWhoseIdentityIsStillPendingWithTheRegistrationDetails() {
        PatientRegistrationRequestDTO adult = request("Naomi Kendi", IdentityDocumentType.PENDING, null);
        adult.setDateOfBirth(LocalDate.of(1987, 2, 8));
        PatientRegistrationResultDTO result = patientRegistrationService.register(adult);
        remember(result);

        assertThat(patientRegistrationService.identityPendingWorklist())
            .filteredOn(entry -> entry.patientId().equals(result.getPatient().getId()))
            .singleElement()
            .satisfies(entry -> {
                assertThat(entry.hospitalId()).isEqualTo(result.getPatient().getHospitalId());
                assertThat(entry.fullName()).isEqualTo("Naomi Kendi");
                assertThat(entry.registeredAt()).isNotNull();
                assertThat(entry.registeredBy()).isEqualTo("admin");
            });
    }

    /** A child is not on the worklist: the rule does not require a document below the age line, so there is nothing to chase. */
    @Test
    void doesNotListAMinorWhoseIdentityIsPending() {
        PatientRegistrationRequestDTO child = request("Purity Wanjiru", IdentityDocumentType.PENDING, null);
        child.setDateOfBirth(LocalDate.of(2019, 6, 1));
        PatientRegistrationResultDTO result = patientRegistrationService.register(child);
        remember(result);

        assertThat(patientRegistrationService.identityPendingWorklist())
            .noneMatch(entry -> entry.patientId().equals(result.getPatient().getId()));
    }

    private void remember(PatientRegistrationResultDTO result) {
        createdPatientIds.add(result.getPatient().getId());
    }

    private static PatientRegistrationRequestDTO request(String fullName, IdentityDocumentType type, String number) {
        PatientRegistrationRequestDTO request = new PatientRegistrationRequestDTO();
        request.setFullName(fullName);
        request.setSex(Sex.FEMALE);
        request.setSexEstimated(false);
        request.setIdentityDocumentType(type);
        request.setIdentityDocumentNumber(number);
        return request;
    }
}
