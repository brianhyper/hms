package com.hyperbrains.hms.service.rules;

import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.domain.enumeration.PrescriptionStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * The prescription state machine.
 *
 * <p>Worth testing directly because the guarantee it encodes is the one Finance and Pharmacy rely on
 * without checking anything themselves: medicine cannot leave the pharmacy before its bill is paid.
 * If {@code afterPayment} ever returned something earlier than {@code READY_FOR_DISPENSE}, or if
 * anything other than payment could reach {@code READY_FOR_DISPENSE}, that guarantee is gone and no
 * other test would notice.
 */
class PrescriptionLifecycleTest {

    @Test
    void aPrescriptionStartsOutstandingPayment() {
        assertThat(PrescriptionLifecycle.initialStatus(false)).isEqualTo(PrescriptionStatus.PENDING);
    }

    /** A late prescription for a patient already at the desk joins what is being collected now. */
    @Test
    void aPrescriptionAddedAtThePaymentDeskStartsPayableImmediately() {
        assertThat(PrescriptionLifecycle.initialStatus(true)).isEqualTo(PrescriptionStatus.PENDING_PAYMENT);
    }

    @Test
    void reachingThePaymentStagePromotesAPendingPrescriptionExactlyOnce() {
        assertThat(PrescriptionLifecycle.promote(PrescriptionStatus.PENDING)).isEqualTo(PrescriptionStatus.PENDING_PAYMENT);
        // Idempotent: the visit can land on the payment stage more than once.
        assertThat(PrescriptionLifecycle.promote(PrescriptionStatus.PENDING_PAYMENT)).isEqualTo(PrescriptionStatus.PENDING_PAYMENT);
    }

    /** Promotion must never resurrect or undo work already done at the pharmacy. */
    @Test
    void promotionLeavesDispensingWorkAlone() {
        assertThat(PrescriptionLifecycle.promote(PrescriptionStatus.READY_FOR_DISPENSE)).isEqualTo(
            PrescriptionStatus.READY_FOR_DISPENSE
        );
        assertThat(PrescriptionLifecycle.promote(PrescriptionStatus.DISPENSED)).isEqualTo(PrescriptionStatus.DISPENSED);
    }

    @Test
    void onlyMoneyOutstandingCanBePaid() {
        assertThat(PrescriptionLifecycle.canBePaid(PrescriptionStatus.PENDING)).isTrue();
        assertThat(PrescriptionLifecycle.canBePaid(PrescriptionStatus.PENDING_PAYMENT)).isTrue();
        assertThat(PrescriptionLifecycle.canBePaid(PrescriptionStatus.READY_FOR_DISPENSE)).isFalse();
        assertThat(PrescriptionLifecycle.canBePaid(PrescriptionStatus.DISPENSED)).isFalse();
    }

    @Test
    void paymentIsWhatReleasesMedicineToThePharmacy() {
        assertThat(PrescriptionLifecycle.afterPayment(PrescriptionStatus.PENDING)).isEqualTo(PrescriptionStatus.READY_FOR_DISPENSE);
        assertThat(PrescriptionLifecycle.afterPayment(PrescriptionStatus.PENDING_PAYMENT)).isEqualTo(PrescriptionStatus.READY_FOR_DISPENSE);
    }

    /**
     * A payment arriving after the medicine was already handed over must not put it back in the queue
     * and invite a second hand-over — that would be stock loss with a paper trail that looks correct.
     */
    @Test
    void aLatePaymentDoesNotRequeueMedicineAlreadyHandedOver() {
        assertThat(PrescriptionLifecycle.afterPayment(PrescriptionStatus.READY_FOR_DISPENSE)).isEqualTo(
            PrescriptionStatus.READY_FOR_DISPENSE
        );
        assertThat(PrescriptionLifecycle.afterPayment(PrescriptionStatus.PARTIALLY_DISPENSED)).isEqualTo(
            PrescriptionStatus.PARTIALLY_DISPENSED
        );
        assertThat(PrescriptionLifecycle.afterPayment(PrescriptionStatus.DISPENSED)).isEqualTo(PrescriptionStatus.DISPENSED);
    }

    /** Nothing that has not been paid for may be handed over. */
    @ParameterizedTest
    @EnumSource(value = PrescriptionStatus.class, names = { "PENDING", "PENDING_PAYMENT" })
    void unpaidPrescriptionsAreNotDispensable(PrescriptionStatus status) {
        assertThat(PrescriptionLifecycle.isDispensable(status)).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = PrescriptionStatus.class, names = { "READY_FOR_DISPENSE", "PARTIALLY_DISPENSED" })
    void paidPrescriptionsAreDispensable(PrescriptionStatus status) {
        assertThat(PrescriptionLifecycle.isDispensable(status)).isTrue();
    }

    @Test
    void onlyAFullyHandedOverPrescriptionIsComplete() {
        assertThat(PrescriptionLifecycle.isComplete(PrescriptionStatus.DISPENSED)).isTrue();
        // Partly handed over is still work outstanding for the pharmacy.
        assertThat(PrescriptionLifecycle.isComplete(PrescriptionStatus.PARTIALLY_DISPENSED)).isFalse();
        assertThat(PrescriptionLifecycle.isComplete(PrescriptionStatus.READY_FOR_DISPENSE)).isFalse();
    }

    /**
     * The two sets drive different queries — "what is owed" and "what may be handed over" — so an
     * overlap would mean a prescription counted as both.
     */
    @Test
    void awaitingPaymentAndDispensableNeverOverlap() {
        assertThat(PrescriptionLifecycle.AWAITING_PAYMENT).doesNotContainAnyElementsOf(PrescriptionLifecycle.DISPENSABLE);
    }

    /**
     * Withdrawal exists to give reserved stock back. It must stop the moment any of it has left the
     * shelf, because at that point there is nothing left to give back.
     */
    @Test
    void onlyUnpaidUndispensedPrescriptionsCanBeWithdrawn() {
        assertThat(PrescriptionLifecycle.isCancellable(PrescriptionStatus.PENDING)).isTrue();
        assertThat(PrescriptionLifecycle.isCancellable(PrescriptionStatus.PENDING_PAYMENT)).isTrue();

        assertThat(PrescriptionLifecycle.isCancellable(PrescriptionStatus.READY_FOR_DISPENSE)).isFalse();
        assertThat(PrescriptionLifecycle.isCancellable(PrescriptionStatus.PARTIALLY_DISPENSED)).isFalse();
        assertThat(PrescriptionLifecycle.isCancellable(PrescriptionStatus.DISPENSED)).isFalse();
        // Idempotent: withdrawing twice is refused rather than releasing the stock twice.
        assertThat(PrescriptionLifecycle.isCancellable(PrescriptionStatus.CANCELLED)).isFalse();
    }

    /**
     * A withdrawn prescription must be inert. If payment or promotion could move it, medicine that
     * was written off would come back into the dispensing queue and its stock would be reserved
     * twice over.
     */
    @Test
    void aWithdrawnPrescriptionIsNeverDispensablePayableOrPromoted() {
        assertThat(PrescriptionLifecycle.isDispensable(PrescriptionStatus.CANCELLED)).isFalse();
        assertThat(PrescriptionLifecycle.canBePaid(PrescriptionStatus.CANCELLED)).isFalse();
        assertThat(PrescriptionLifecycle.afterPayment(PrescriptionStatus.CANCELLED)).isEqualTo(PrescriptionStatus.CANCELLED);
        assertThat(PrescriptionLifecycle.promote(PrescriptionStatus.CANCELLED)).isEqualTo(PrescriptionStatus.CANCELLED);
    }

    /**
     * The pharmacy queue is driven by {@code DISPENSABLE}, so this is the assertion that keeps
     * withdrawn medicine out of the counter's hands without a second guard elsewhere.
     */
    @Test
    void theDispensingQueueNeverContainsAWithdrawnPrescription() {
        assertThat(PrescriptionLifecycle.DISPENSABLE).doesNotContain(PrescriptionStatus.CANCELLED);
    }

    /** {@code PAID} is a generated value Phase 1 never produces; nothing may route to it by accident. */
    @Test
    void noTransitionIntroducesPaid() {
        for (PrescriptionStatus status : PrescriptionStatus.values()) {
            if (status == PrescriptionStatus.PAID) {
                // There is no Phase 1 path into it, so there is nothing to transition from it either.
                continue;
            }
            assertThat(PrescriptionLifecycle.promote(status)).as("promote(%s)", status).isNotEqualTo(PrescriptionStatus.PAID);
            assertThat(PrescriptionLifecycle.afterPayment(status)).as("afterPayment(%s)", status).isNotEqualTo(PrescriptionStatus.PAID);
        }
    }
}
