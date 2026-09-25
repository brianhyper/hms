package com.hyperbrains.hms.service.rules;

import com.hyperbrains.hms.domain.enumeration.PrescriptionStatus;
import java.util.Set;

/**
 * The prescription's own state machine and who is allowed to move it.
 *
 * <p>Two stations advance the same record from different places — Finance pays the bill, Pharmacy
 * hands over the medicine — and the status is the only thing telling them whose turn it is. The
 * point of the cycle is the guarantee the specification asks for: <em>a prescription can only reach
 * the pharmacy's dispensing queue after its bill is paid</em>, so Pharmacy never has to look at the
 * Visit or the Bill to know whether it is safe to hand the medicine over.
 *
 * <pre>
 *   PENDING ──visit reaches the payment stage──▶ PENDING_PAYMENT ──bill paid──▶ READY_FOR_DISPENSE
 *                                                                                      │
 *                                                            partial hand-over ◀───────┤
 *                                                                      │               │
 *                                                       PARTIALLY_DISPENSED ──▶ DISPENSED
 * </pre>
 *
 * <p>{@code PAID} is deliberately not produced by this class. It is a value the generated domain
 * already carries, but Phase 1 has no case for it: a settled prescription always has medicine to
 * hand over, so it goes to {@code READY_FOR_DISPENSE}. See the note on {@link #afterPayment}.
 */
public final class PrescriptionLifecycle {

    private PrescriptionLifecycle() {}

    /** Neither paid nor dispensed: the money leg is still outstanding. */
    public static final Set<PrescriptionStatus> AWAITING_PAYMENT = Set.of(
        PrescriptionStatus.PENDING,
        PrescriptionStatus.PENDING_PAYMENT
    );

    /** Safe to hand over, in whole or in part. This is what the pharmacy queue shows. */
    public static final Set<PrescriptionStatus> DISPENSABLE = Set.of(
        PrescriptionStatus.READY_FOR_DISPENSE,
        PrescriptionStatus.PARTIALLY_DISPENSED
    );

    /**
     * The status a new prescription starts in.
     *
     * <p>Normally {@code PENDING}: the stock is already set aside, but the visit is still working
     * through consultations and tests, so there is nothing for Finance to collect yet. If the visit
     * has already reached the payment stage — a prescription added to a patient who is at the desk —
     * it joins what is being collected straight away.
     *
     * @param visitAlreadyAtPaymentStage the visit is in {@code WAITING_PAYMENT}
     */
    public static PrescriptionStatus initialStatus(boolean visitAlreadyAtPaymentStage) {
        return visitAlreadyAtPaymentStage ? PrescriptionStatus.PENDING_PAYMENT : PrescriptionStatus.PENDING;
    }

    /**
     * The visit has reached the payment stage, so this prescription is now part of what is owed.
     *
     * <p>Idempotent: called whenever the visit lands on the payment stage, which can happen more than
     * once as work is added and resolved.
     */
    public static PrescriptionStatus promote(PrescriptionStatus current) {
        return current == PrescriptionStatus.PENDING ? PrescriptionStatus.PENDING_PAYMENT : current;
    }

    /** Whether this prescription's charge can be settled. */
    public static boolean canBePaid(PrescriptionStatus status) {
        return AWAITING_PAYMENT.contains(status);
    }

    /**
     * The bill this prescription belongs to has been paid, so pharmacy may hand it over.
     *
     * <p>Answers {@code READY_FOR_DISPENSE}, which is the explicit signal the specification asks for
     * rather than making pharmacy infer it from the Visit or Bill. {@code PAID} is never returned:
     * every Phase 1 prescription has medicine to collect, so a settled one is always something to
     * dispense. A prescription that is genuinely nothing to dispense would be a new requirement, not
     * a silent reinterpretation of that value.
     *
     * <p>Only the two awaiting-payment states advance. Everything from {@code READY_FOR_DISPENSE} on
     * is left exactly as it was, because a payment recorded twice — or recorded late, after the
     * counter has already handed some of the medicine over — must never put medicine back in the
     * queue and invite a second hand-over.
     */
    public static PrescriptionStatus afterPayment(PrescriptionStatus current) {
        return AWAITING_PAYMENT.contains(current) ? PrescriptionStatus.READY_FOR_DISPENSE : current;
    }

    /** Whether pharmacy may hand this over, in whole or in part. */
    public static boolean isDispensable(PrescriptionStatus status) {
        return DISPENSABLE.contains(status);
    }

    /**
     * Whether the prescription can still be withdrawn.
     *
     * <p>Only before payment. Once the money has been taken the medicine is owed, so undoing it is a
     * refund — a Finance act, not a prescribing one — and refusing here keeps money out of a
     * clinical endpoint, exactly as a completed diagnostic order cannot be cancelled.
     *
     * <p>Withdrawing is what returns reserved stock to the shelf. Without it, a prescription for a
     * patient who never comes back holds its reservation for ever and the availability figure drifts
     * permanently away from reality.
     */
    public static boolean isCancellable(PrescriptionStatus status) {
        return AWAITING_PAYMENT.contains(status);
    }

    /** Nothing left to do: fully handed over. */
    public static boolean isComplete(PrescriptionStatus status) {
        return status == PrescriptionStatus.DISPENSED;
    }
}
