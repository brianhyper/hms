package com.hyperbrains.hms.service.rules;

import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.domain.enumeration.IdentityDocumentType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the duplicate rules. Pure logic, so these run without a database and cover the
 * cases that are hard to reproduce through the API.
 */
class PatientDuplicateMatcherTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 9, 24);

    private static final PatientDuplicateMatcher.Settings SETTINGS = new PatientDuplicateMatcher.Settings(80, 95, 2);

    @Test
    void exactMatchIgnoresCaseSpacingAndPunctuation() {
        var candidate = new PatientDuplicateMatcher.Candidate(
            "John Kamau",
            null,
            null,
            null,
            IdentityDocumentType.NATIONAL_ID,
            "123-456-78"
        );
        var existing = existing(1L, "HMS-2026-0001", "Someone Else", null, null, null, IdentityDocumentType.NATIONAL_ID, "12345678");

        Optional<PatientDuplicateMatcher.Match> match = PatientDuplicateMatcher.exactIdentityDocumentMatch(
            candidate,
            List.of(existing)
        );

        assertThat(match).isPresent();
        assertThat(match.orElseThrow().patient().hospitalId()).isEqualTo("HMS-2026-0001");
        assertThat(match.orElseThrow().reasons()).containsExactly(PatientDuplicateMatcher.Reason.SAME_IDENTITY_DOCUMENT);
    }

    @Test
    void exactMatchIsNotAttemptedWithoutADocumentTypeOrNumber() {
        var noType = new PatientDuplicateMatcher.Candidate("John Kamau", null, null, null, null, "12345678");
        var noNumber = new PatientDuplicateMatcher.Candidate("John Kamau", null, null, null, IdentityDocumentType.NATIONAL_ID, "  ");
        var existing = existing(1L, "HMS-2026-0001", "John Kamau", null, null, null, IdentityDocumentType.NATIONAL_ID, "12345678");

        assertThat(PatientDuplicateMatcher.exactIdentityDocumentMatch(noType, List.of(existing))).isEmpty();
        assertThat(PatientDuplicateMatcher.exactIdentityDocumentMatch(noNumber, List.of(existing))).isEmpty();
    }

    @Test
    void differentDocumentTypesDoNotMatch() {
        var candidate = new PatientDuplicateMatcher.Candidate(
            "John Kamau",
            null,
            null,
            null,
            IdentityDocumentType.PASSPORT,
            "12345678"
        );
        var existing = existing(1L, "HMS-2026-0001", "John Kamau", null, null, null, IdentityDocumentType.NATIONAL_ID, "12345678");

        assertThat(PatientDuplicateMatcher.exactIdentityDocumentMatch(candidate, List.of(existing))).isEmpty();
    }

    /**
     * A similar name on its own is not enough. Otherwise a common name would bury Reception in
     * false positives, which trains people to dismiss the warning.
     */
    @Test
    void aSimilarNameAloneIsNotSurfaced() {
        var candidate = new PatientDuplicateMatcher.Candidate("John Kamau", LocalDate.of(1990, 1, 1), null, "0700000001", null, null);
        // 1 edit apart, so similar but below the "strong on its own" threshold, and nothing else matches.
        var existing = existing(1L, "HMS-2026-0001", "John Kamaus", null, null, null, null, null);

        assertThat(PatientDuplicateMatcher.possibleDuplicates(candidate, List.of(existing), SETTINGS, TODAY)).isEmpty();
    }

    @Test
    void aSimilarNameCorroboratedByDateOfBirthIsSurfaced() {
        var candidate = new PatientDuplicateMatcher.Candidate("John Kamau", LocalDate.of(1990, 1, 1), null, "0700000001", null, null);
        var existing = existing(1L, "HMS-2026-0001", "John Kamaus", LocalDate.of(1990, 1, 1), null, null, null, null);

        List<PatientDuplicateMatcher.Match> matches = PatientDuplicateMatcher.possibleDuplicates(
            candidate,
            List.of(existing),
            SETTINGS,
            TODAY
        );

        assertThat(matches).hasSize(1);
        assertThat(matches.getFirst().reasons()).containsExactlyInAnyOrder(
            PatientDuplicateMatcher.Reason.SIMILAR_NAME,
            PatientDuplicateMatcher.Reason.SAME_DATE_OF_BIRTH
        );
    }

    @Test
    void aSimilarNameCorroboratedByPhoneIsSurfaced() {
        var candidate = new PatientDuplicateMatcher.Candidate("John Kamau", null, null, "0700 000 001", null, null);
        var existing = existing(1L, "HMS-2026-0001", "Jon Kamau", null, null, "0700000001", null, null);

        List<PatientDuplicateMatcher.Match> matches = PatientDuplicateMatcher.possibleDuplicates(
            candidate,
            List.of(existing),
            SETTINGS,
            TODAY
        );

        assertThat(matches).hasSize(1);
        assertThat(matches.getFirst().reasons()).contains(PatientDuplicateMatcher.Reason.SAME_PHONE);
    }

    /** A patient re-registered under a different name is exactly what the strong-name rule catches. */
    @Test
    void anAlmostIdenticalNameIsSurfacedWithNoOtherSignal() {
        var candidate = new PatientDuplicateMatcher.Candidate("John Kamau", null, null, null, null, null);
        var existing = existing(1L, "HMS-2026-0001", "John Kamau", null, null, null, null, null);

        List<PatientDuplicateMatcher.Match> matches = PatientDuplicateMatcher.possibleDuplicates(
            candidate,
            List.of(existing),
            SETTINGS,
            TODAY
        );

        assertThat(matches).hasSize(1);
        assertThat(matches.getFirst().nameSimilarity()).isEqualTo(100);
    }

    @Test
    void estimatedAgesAreComparedOnlyWhenNeeded() {
        var candidate = new PatientDuplicateMatcher.Candidate("Grace Wanjiru", null, 30, null, null, null);
        var nearby = existing(1L, "HMS-2026-0001", "Grace Wanjiru", null, 31, null, null, null);
        var farOff = existing(2L, "HMS-2026-0002", "Grace Wanjiru", null, 45, null, null, null);

        List<PatientDuplicateMatcher.Match> nearbyMatches = PatientDuplicateMatcher.possibleDuplicates(
            candidate,
            List.of(nearby),
            SETTINGS,
            TODAY
        );
        List<PatientDuplicateMatcher.Match> farMatches = PatientDuplicateMatcher.possibleDuplicates(
            candidate,
            List.of(farOff),
            SETTINGS,
            TODAY
        );

        assertThat(nearbyMatches.getFirst().reasons()).contains(PatientDuplicateMatcher.Reason.SIMILAR_AGE);
        assertThat(farMatches.getFirst().reasons()).doesNotContain(PatientDuplicateMatcher.Reason.SIMILAR_AGE);
    }

    @Test
    void resultsAreOrderedBySimilarity() {
        var candidate = new PatientDuplicateMatcher.Candidate("John Kamau", LocalDate.of(1990, 1, 1), null, "0700000001", null, null);
        var close = existing(1L, "HMS-2026-0001", "John Kamaus", LocalDate.of(1990, 1, 1), null, null, null, null);
        var further = existing(2L, "HMS-2026-0002", "Jon Kamao", LocalDate.of(1990, 1, 1), null, null, null, null);

        List<PatientDuplicateMatcher.Match> matches = PatientDuplicateMatcher.possibleDuplicates(
            candidate,
            List.of(further, close),
            SETTINGS,
            TODAY
        );

        assertThat(matches).extracting(PatientDuplicateMatcher.Match::nameSimilarity).isSortedAccordingTo(java.util.Comparator.reverseOrder());
    }

    @Test
    void normalizationIgnoresFormattingNoise() {
        assertThat(PatientDuplicateMatcher.normalizeName("  John   KAMAU  ")).isEqualTo("john kamau");
        assertThat(PatientDuplicateMatcher.normalizeName("O'Brien-Kamau")).isEqualTo("o brien kamau");
        assertThat(PatientDuplicateMatcher.normalizePhone("+254 712-345 678")).isEqualTo("254712345678");
        assertThat(PatientDuplicateMatcher.normalizeDocumentNumber("123-456/78")).isEqualTo("12345678");
        assertThat(PatientDuplicateMatcher.normalizeName(null)).isEmpty();
        assertThat(PatientDuplicateMatcher.normalizePhone(null)).isEmpty();
    }

    @Test
    void leadingNameTokenFeedsTheSqlPrefilter() {
        assertThat(PatientDuplicateMatcher.leadingNameToken("  John   Kamau ")).isEqualTo("john");
        assertThat(PatientDuplicateMatcher.leadingNameToken("Kamau")).isEqualTo("kamau");
        assertThat(PatientDuplicateMatcher.leadingNameToken(null)).isEmpty();
    }

    @Test
    void nameSimilarityIsSymmetric() {
        assertThat(PatientDuplicateMatcher.nameSimilarity("john kamau", "john kamao")).isEqualTo(
            PatientDuplicateMatcher.nameSimilarity("john kamao", "john kamau")
        );
        assertThat(PatientDuplicateMatcher.nameSimilarity("john kamau", "john kamau")).isEqualTo(100);
        assertThat(PatientDuplicateMatcher.nameSimilarity("", "")).isEqualTo(100);
        assertThat(PatientDuplicateMatcher.nameSimilarity("john", "")).isZero();
    }

    private static PatientDuplicateMatcher.ExistingPatient existing(
        Long id,
        String hospitalId,
        String fullName,
        LocalDate dateOfBirth,
        Integer estimatedAge,
        String phone,
        IdentityDocumentType documentType,
        String documentNumber
    ) {
        return new PatientDuplicateMatcher.ExistingPatient(
            id,
            hospitalId,
            fullName,
            dateOfBirth,
            estimatedAge,
            phone,
            documentType,
            documentNumber
        );
    }
}
