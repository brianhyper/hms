package com.hyperbrains.hms.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.service.rules.PatientFields;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * The field diff and the field classification tables.
 *
 * <p>The diff is what keeps the correction history honest in both directions, so its edges are worth
 * pinning down: log too much and the real change is buried, log too little and the trail is a false record.
 */
class AuditFieldChangeTest {

    @Test
    void onlyTheFieldsThatChangedAreReported() {
        Map<String, String> before = snapshot("phone", "0700000000", "email", "a@b.c");
        Map<String, String> after = snapshot("phone", "0711111111", "email", "a@b.c");

        assertThat(AuditLogService.FieldChange.diff(before, after))
            .singleElement()
            .satisfies(change -> {
                assertThat(change.field()).isEqualTo("phone");
                assertThat(change.previousValue()).isEqualTo("0700000000");
                assertThat(change.currentValue()).isEqualTo("0711111111");
            });
    }

    @Test
    void anIdenticalSnapshotReportsNothing() {
        Map<String, String> same = snapshot("phone", "0700000000", "email", "a@b.c");

        assertThat(AuditLogService.FieldChange.diff(same, snapshot("phone", "0700000000", "email", "a@b.c"))).isEmpty();
    }

    /**
     * A field that was empty and is still empty has not changed, and a row saying it did would be a false
     * record. Whitespace counts as empty for the same reason.
     */
    @Test
    void absentBlankAndWhitespaceAreAllTheSameValue() {
        Map<String, String> before = snapshot("phone", null, "email", "");
        Map<String, String> after = snapshot("phone", "", "email", "   ");

        assertThat(AuditLogService.FieldChange.diff(before, after)).isEmpty();
    }

    @Test
    void fillingInAnEmptyFieldIsAChange() {
        Map<String, String> before = snapshot("phone", null);
        Map<String, String> after = snapshot("phone", "0700000000");

        assertThat(AuditLogService.FieldChange.diff(before, after))
            .singleElement()
            .satisfies(change -> {
                assertThat(change.previousValue()).isNull();
                assertThat(change.currentValue()).isEqualTo("0700000000");
            });
    }

    /** Clearing a field is a change too — that is how a value entered against the wrong patient is removed. */
    @Test
    void emptyingAFieldIsAChange() {
        Map<String, String> before = snapshot("phone", "0700000000");
        Map<String, String> after = snapshot("phone", null);

        assertThat(AuditLogService.FieldChange.diff(before, after))
            .singleElement()
            .satisfies(change -> assertThat(change.currentValue()).isNull());
    }

    /** The newer snapshot drives the report, so the history reads in the order the record is laid out. */
    @Test
    void reportedFieldsFollowTheLaterSnapshotOrder() {
        Map<String, String> before = snapshot("email", "old@b.c", "phone", "0700000000");
        Map<String, String> after = snapshot("email", "new@b.c", "phone", "0711111111");

        assertThat(AuditLogService.FieldChange.diff(before, after))
            .extracting(AuditLogService.FieldChange::field)
            .containsExactly("email", "phone");
    }

    /** A field the later snapshot does not mention is not part of this correction at all. */
    @Test
    void aFieldMissingFromTheLaterSnapshotIsIgnored() {
        Map<String, String> before = snapshot("phone", "0700000000", "email", "a@b.c");
        Map<String, String> after = snapshot("phone", "0711111111");

        assertThat(AuditLogService.FieldChange.diff(before, after))
            .extracting(AuditLogService.FieldChange::field)
            .containsExactly("phone");
    }

    @Test
    void aSnapshotOfNothingChangesNothing() {
        assertThat(AuditLogService.FieldChange.diff(Map.of(), Map.of())).isEmpty();
    }

    // ---------------------------------------------------------------- the classification tables

    /**
     * The two groups drive different authority checks, so an overlap would mean a field that is both
     * the desk's to correct and clinical.
     */
    @Test
    void demographicAndClinicalFieldsNeverOverlap() {
        assertThat(PatientFields.DEMOGRAPHIC).doesNotContainAnyElementsOf(PatientFields.CLINICAL);
    }

    /**
     * A field could otherwise be added to the correctable set and never appear in the snapshot, which
     * would mean correcting it leaves no trace. The reverse is caught by the integration test.
     */
    @Test
    void theNeverCorrectableFieldsAreNotAlsoCorrectable() {
        assertThat(PatientFields.NEVER_CORRECTABLE).doesNotContainAnyElementsOf(PatientFields.DEMOGRAPHIC);
        assertThat(PatientFields.NEVER_CORRECTABLE).doesNotContainAnyElementsOf(PatientFields.CLINICAL);
    }

    @Test
    void knownFieldsAreClassifiedAndUnknownOnesAreNot() {
        assertThat(PatientFields.isKnown("phone")).isTrue();
        assertThat(PatientFields.isKnown("knownAllergies")).isTrue();
        assertThat(PatientFields.isClinical("knownAllergies")).isTrue();
        assertThat(PatientFields.isClinical("phone")).isFalse();
        // The permanent identifier is not correctable, by anyone, ever.
        assertThat(PatientFields.isKnown("hospitalId")).isFalse();
    }

    /** A map that keeps the order it was given, so the ordering assertions above mean something. */
    private static Map<String, String> snapshot(String... alternatingKeyAndValue) {
        Map<String, String> values = new LinkedHashMap<>();
        for (int i = 0; i < alternatingKeyAndValue.length; i += 2) {
            values.put(alternatingKeyAndValue[i], alternatingKeyAndValue[i + 1]);
        }
        return values;
    }
}
