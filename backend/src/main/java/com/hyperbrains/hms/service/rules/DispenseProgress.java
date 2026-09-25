package com.hyperbrains.hms.service.rules;

import com.hyperbrains.hms.domain.enumeration.PrescriptionStatus;
import java.util.Collection;

/**
 * How far through handing medicine over the pharmacy is.
 *
 * <p>Dispensing is not one act. A counter may hand over what it has now and the rest when the delivery
 * arrives, which is why {@code Dispense} is a transaction rather than a flag on the prescription. The
 * state of the prescription is therefore derived from the quantities already handed over, never set
 * by hand, because a status set by hand is a status that eventually disagrees with the record of what
 * actually left the shelf.
 */
public final class DispenseProgress {

    private DispenseProgress() {}

    /** What is still to hand over on one line. */
    public static int remaining(int ordered, int dispensed) {
        int left = ordered - dispensed;
        return Math.max(left, 0);
    }

    /**
     * Whether this hand-over asks for more than is left on that line.
     *
     * <p>Guarded explicitly rather than clamped: silently reducing the quantity to what is left would
     * let a mistyped 30 become a 3 and look like a successful dispense.
     */
    public static boolean exceedsRemaining(int ordered, int dispensed, int requested) {
        return requested > remaining(ordered, dispensed);
    }

    /**
     * The prescription's status once this hand-over is recorded.
     *
     * <p>{@code DISPENSED} only when nothing at all is left. Anything else is
     * {@code PARTIALLY_DISPENSED}, including a prescription where one line is complete and another has
     * not been touched — the patient still has medicine to collect, and marking it DISPENSED would take
     * it off the queue and leave the rest owed but unasked-for.
     */
    public static PrescriptionStatus statusFor(Collection<Integer> remainingQuantities) {
        return remainingQuantities.stream().allMatch(remaining -> remaining == 0)
            ? PrescriptionStatus.DISPENSED
            : PrescriptionStatus.PARTIALLY_DISPENSED;
    }
}
