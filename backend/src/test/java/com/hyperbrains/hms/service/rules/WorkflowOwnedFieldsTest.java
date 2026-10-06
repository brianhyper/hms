package com.hyperbrains.hms.service.rules;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * The shared guard behind Phase 3's domain-operation rule.
 *
 * <p>The last test is the important one: a guard that names a field which does not exist protects nothing at
 * all while looking exactly like a guard that does, so it has to fail rather than pass.
 */
class WorkflowOwnedFieldsTest {

    /** Stands in for a workflow-owned row: a status, an amount, and free text nobody's rules depend on. */
    static final class Row {

        @SuppressWarnings("unused")
        private String status;

        @SuppressWarnings("unused")
        private BigDecimal amount;

        @SuppressWarnings("unused")
        private String notes;

        Row(String status, BigDecimal amount, String notes) {
            this.status = status;
            this.amount = amount;
            this.notes = notes;
        }
    }

    @Test
    void reportsTheFieldsThatChanged() {
        Row stored = new Row("ADMITTED", new BigDecimal("3500.00"), "noted");
        Row requested = new Row("DISCHARGED", new BigDecimal("3500.00"), "noted");

        assertThat(WorkflowOwnedFields.changed(requested, stored, false, "status", "amount")).containsExactly("status");
    }

    @Test
    void reportsNothingWhenNothingChanged() {
        Row stored = new Row("ADMITTED", new BigDecimal("3500.00"), "noted");
        Row requested = new Row("ADMITTED", new BigDecimal("3500.00"), "noted");

        assertThat(WorkflowOwnedFields.changed(requested, stored, false, "status", "amount")).isEmpty();
    }

    @Test
    void aRateIsTheSameRateWhateverTheScaleSays() {
        Row stored = new Row("ADMITTED", new BigDecimal("8000.00"), "noted");
        Row requested = new Row("ADMITTED", new BigDecimal("8000.0"), "noted");

        assertThat(WorkflowOwnedFields.changed(requested, stored, false, "amount")).isEmpty();
    }

    @Test
    void aDifferentRateIsStillADifference() {
        Row stored = new Row("ADMITTED", new BigDecimal("8000.00"), "noted");
        Row requested = new Row("ADMITTED", new BigDecimal("1.00"), "noted");

        assertThat(WorkflowOwnedFields.changed(requested, stored, false, "amount")).containsExactly("amount");
    }

    @Test
    void anAbsentFieldMeansLeaveItAloneOnAPatchAndClearItOnAPut() {
        Row stored = new Row("ADMITTED", new BigDecimal("3500.00"), "noted");
        Row requested = new Row(null, null, null);

        assertThat(WorkflowOwnedFields.changed(requested, stored, true, "status", "amount"))
            .as("a PATCH that says nothing about them is not changing them")
            .isEmpty();
        assertThat(WorkflowOwnedFields.changed(requested, stored, false, "status", "amount"))
            .as("a PUT that leaves them out would clear them")
            .containsExactly("status", "amount");
    }

    @Test
    void aFieldThatDoesNotExistFailsRatherThanProtectingNothing() {
        Row stored = new Row("ADMITTED", new BigDecimal("3500.00"), "noted");

        assertThatThrownBy(() -> WorkflowOwnedFields.changed(stored, stored, false, "statuss"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("statuss");
    }

    @Test
    void theSameRowComparedWithItselfNeverChanges() {
        Row stored = new Row("ADMITTED", new BigDecimal("3500.00"), "noted");

        assertThat(WorkflowOwnedFields.changed(stored, stored, false, "status", "amount", "notes")).isEqualTo(List.of());
    }

    @Test
    void aReferenceIsTheSameReferenceWhenTheIdIsTheSame() {
        assertThat(WorkflowOwnedFields.referenceChanged(42L, 42L, false)).isFalse();
        assertThat(WorkflowOwnedFields.referenceChanged(42L, 43L, false)).isTrue();
    }

    @Test
    void anAbsentReferenceMeansLeaveItAloneOnAPatchAndClearItOnAPut() {
        assertThat(WorkflowOwnedFields.referenceChanged(null, 42L, true))
            .as("a PATCH that says nothing about it is not re-pointing it")
            .isFalse();
        assertThat(WorkflowOwnedFields.referenceChanged(null, 42L, false))
            .as("a PUT that leaves it out would clear it")
            .isTrue();
    }

    @Test
    void anUnsetReferenceIsNotARepoint() {
        assertThat(WorkflowOwnedFields.referenceChanged(null, null, false)).isFalse();
    }
}
