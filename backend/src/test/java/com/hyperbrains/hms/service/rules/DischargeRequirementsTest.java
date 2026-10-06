package com.hyperbrains.hms.service.rules;

import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.domain.enumeration.BillStatus;
import org.junit.jupiter.api.Test;

/** Pure rules for discharging a stay. No database involved. */
class DischargeRequirementsTest {

    private static final Long DOCTOR = 7L;
    private static final Long NURSE = 9L;

    @Test
    void aDischargeIsSignedOffWhenBothPeopleHaveSignedIt() {
        assertThat(DischargeRequirements.isSignedOff(DOCTOR, NURSE)).isTrue();
    }

    @Test
    void oneSignatureIsNotADischarge() {
        assertThat(DischargeRequirements.isSignedOff(DOCTOR, null)).as("the nurse has not signed").isFalse();
        assertThat(DischargeRequirements.isSignedOff(null, NURSE)).as("the doctor has not signed").isFalse();
        assertThat(DischargeRequirements.isSignedOff(null, null)).isFalse();
    }

    /**
     * The specification's argument, made into a rule: one endpoint taking two names would let a single caller claim
     * both sign-offs, which is the thing the second signature exists to prevent. Letting one person do both is a
     * decision to record, not an accident to allow, so the rule refuses it.
     */
    @Test
    void theSamePersonCannotBeBothSignatures() {
        assertThat(DischargeRequirements.isOnePersonSigningTwice(DOCTOR, DOCTOR)).isTrue();
        assertThat(DischargeRequirements.isOnePersonSigningTwice(DOCTOR, NURSE)).isFalse();
        assertThat(DischargeRequirements.isOnePersonSigningTwice(null, null)).isFalse();
    }

    @Test
    void bothOfTheSamePersonsNamesIsNotADischargeEither() {
        assertThat(DischargeRequirements.isSignedOff(DOCTOR, DOCTOR))
            .as("two signatures by one person is one signature")
            .isFalse();
    }

    @Test
    void aSettledBillLetsADischargeBegin() {
        assertThat(DischargeRequirements.moneyIsSettled(BillStatus.PAID, false)).isTrue();
    }

    @Test
    void anUnsettledBillWithNoArrangementStopsIt() {
        assertThat(DischargeRequirements.moneyIsSettled(BillStatus.UNPAID, false))
            .as("the running bill is what the gate is for")
            .isFalse();
    }

    @Test
    void anArrangementToPayCountsAsSettled() {
        assertThat(DischargeRequirements.moneyIsSettled(BillStatus.UNPAID, true))
            .as("holding a patient in a bed until a guarantor pays is not a decision a hospital can take")
            .isTrue();
    }

    /**
     * The case that keeps this slice alive: a stay with no bill owes nothing, and the daily bed-day charge that would
     * create one is not built yet, so refusing over an absent bill would make every discharge impossible.
     */
    @Test
    void aStayWithNoBillAtAllHasNothingOutstanding() {
        assertThat(DischargeRequirements.moneyIsSettled(null, false)).isTrue();
    }
}
