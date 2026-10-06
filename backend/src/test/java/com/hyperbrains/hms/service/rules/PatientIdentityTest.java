package com.hyperbrains.hms.service.rules;

import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.domain.enumeration.IdentityDocumentType;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

/**
 * When an identity document is required, and — just as important — when it is not.
 *
 * <p>The second half is what these tests mostly cover. A rule that refuses the people it was not meant to refuse is
 * how a registration desk ends up recording a document number that nobody checked, or an age that nobody estimated,
 * to get past a form.
 */
class PatientIdentityTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 5);

    @Test
    void anAdultWithNoDocumentIsRefused() {
        assertThat(PatientIdentity.requiresDocumentButHasNone(LocalDate.of(1990, 1, 1), null, null, TODAY)).isTrue();
        assertThat(PatientIdentity.requiresDocumentButHasNone(LocalDate.of(1990, 1, 1), null, "   ", TODAY)).isTrue();
    }

    @Test
    void anAdultWithADocumentIsAccepted() {
        assertThat(PatientIdentity.requiresDocumentButHasNone(LocalDate.of(1990, 1, 1), null, "NID-123", TODAY)).isFalse();
    }

    /** The boundary itself: a patient is required to have one only once they are past nineteen, not at it. */
    @Test
    void nineteenIsNotYetOldEnoughAndTwentyIs() {
        LocalDate twentyYearsAgo = TODAY.minusYears(20);
        LocalDate nineteenYearsAgo = TODAY.minusYears(19);

        assertThat(PatientIdentity.requiresDocument(20)).isTrue();
        assertThat(PatientIdentity.requiresDocument(19)).isFalse();
        assertThat(PatientIdentity.requiresDocumentButHasNone(twentyYearsAgo, null, null, TODAY)).isTrue();
        assertThat(PatientIdentity.requiresDocumentButHasNone(nineteenYearsAgo, null, null, TODAY)).isFalse();
    }

    @Test
    void aChildIsNeverRefusedForNotHavingOne() {
        assertThat(PatientIdentity.requiresDocumentButHasNone(LocalDate.of(2020, 6, 1), null, null, TODAY)).isFalse();
        assertThat(PatientIdentity.requiresDocumentButHasNone(null, 4, null, TODAY)).isFalse();
    }

    /**
     * An age nobody recorded is not an adult age. Refusing here would turn a missing document into a refused
     * registration for the patients whose ages are hardest to establish.
     */
    @Test
    void anUnknownAgeIsNotTreatedAsAnAdult() {
        assertThat(PatientIdentity.ageInYears(null, null, TODAY)).isNull();
        assertThat(PatientIdentity.requiresDocumentButHasNone(null, null, null, TODAY)).isFalse();
    }

    /** The date of birth is what counts when it is there, whatever the estimate alongside it says. */
    @Test
    void theDateOfBirthWinsOverTheEstimate() {
        assertThat(PatientIdentity.ageInYears(LocalDate.of(2015, 1, 1), 60, TODAY)).isEqualTo(11);
        assertThat(PatientIdentity.requiresDocumentButHasNone(LocalDate.of(2015, 1, 1), 60, null, TODAY)).isFalse();
        // and the other way round, where there is no date of birth to prefer
        assertThat(PatientIdentity.ageInYears(null, 60, TODAY)).isEqualTo(60);
        assertThat(PatientIdentity.requiresDocumentButHasNone(null, 60, null, TODAY)).isTrue();
    }

    /** The pending marker is not a document; it is the explicit statement that there is not one yet. */
    @Test
    void thePendingMarkerIsRecognisedAndIsNotADocumentType() {
        assertThat(PatientIdentity.isPending(IdentityDocumentType.PENDING)).isTrue();
        assertThat(PatientIdentity.isPending(IdentityDocumentType.NATIONAL_ID)).isFalse();
        assertThat(PatientIdentity.isPending(null)).isFalse();
    }

    /**
     * The rule counts a pending identity as having no document. That is deliberate: an adult who is marked
     * pending is only accepted because the service takes the marker as the alternative, so the rule alone would
     * still say the document is missing. Silence and the marker must not be the same thing to the rule.
     */
    @Test
    void anAdultWhoIsPendingStillCountsAsHavingNoDocument() {
        assertThat(PatientIdentity.requiresDocumentButHasNone(LocalDate.of(1990, 1, 1), null, null, TODAY)).isTrue();
    }

    @Test
    void aPendingMarkerWithANumberIsAContradiction() {
        assertThat(PatientIdentity.documentTypeAndNumberDisagree(IdentityDocumentType.PENDING, "12345678")).isTrue();
        assertThat(PatientIdentity.documentTypeAndNumberDisagree(IdentityDocumentType.PENDING, null)).isFalse();
        assertThat(PatientIdentity.documentTypeAndNumberDisagree(IdentityDocumentType.PENDING, "   ")).isFalse();
    }

    @Test
    void aNamedDocumentTypeWithNoNumberIsAContradiction() {
        assertThat(PatientIdentity.documentTypeAndNumberDisagree(IdentityDocumentType.NATIONAL_ID, null)).isTrue();
        assertThat(PatientIdentity.documentTypeAndNumberDisagree(IdentityDocumentType.PASSPORT, "  ")).isTrue();
        assertThat(PatientIdentity.documentTypeAndNumberDisagree(IdentityDocumentType.NATIONAL_ID, "12345678")).isFalse();
        // Silence is not a contradiction; whether it is refused depends on the patient's age.
        assertThat(PatientIdentity.documentTypeAndNumberDisagree(null, null)).isFalse();
    }
}
