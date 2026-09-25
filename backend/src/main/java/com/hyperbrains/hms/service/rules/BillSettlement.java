package com.hyperbrains.hms.service.rules;

import java.math.BigDecimal;

/**
 * When a bill has actually been paid.
 *
 * <p>The rule the specification sets is that a bill is only finalised — and therefore only collectable
 * — once the visit reaches the payment stage, at which point every incrementally raised charge has
 * already been summed into the total. From there, the arithmetic is small but it is the arithmetic a
 * patient's money depends on, so it lives here and is tested directly rather than inline in a service.
 *
 * <p>Amounts are only ever compared, never constructed: a rounded or scaled figure invented here would
 * disagree with the stored total.
 */
public final class BillSettlement {

    private BillSettlement() {}

    /**
     * What is still owed, given what has already been recorded against the bill.
     *
     * <p>Never negative: a bill whose total has been reduced below what was collected is a refund
     * situation, not a negative debt, and reporting {@code -50} would have Finance chasing the patient
     * for money the hospital owes them.
     */
    public static BigDecimal outstanding(BigDecimal total, BigDecimal alreadyRecorded) {
        BigDecimal owed = total.subtract(alreadyRecorded == null ? BigDecimal.ZERO : alreadyRecorded);
        return owed.signum() < 0 ? BigDecimal.ZERO : owed;
    }

    /**
     * Whether this payment is the one that settles the bill.
     *
     * <p>Compared by value, not by {@code equals}: {@code 1200.00} from the request and
     * {@code 1200.000} from the database are the same amount of money, and BigDecimal.equals would
     * call them different.
     */
    public static boolean settles(BigDecimal total, BigDecimal alreadyRecorded, BigDecimal amount) {
        BigDecimal collected = (alreadyRecorded == null ? BigDecimal.ZERO : alreadyRecorded).add(amount);
        return collected.compareTo(total) >= 0;
    }

    /**
     * How much this payment would overshoot by, or zero when it fits.
     *
     * <p>Overpayment is refused rather than accepted: it is nearly always a mistyped figure, and a
     * bill holding more than it is owed corrupts every downstream total, including the receipt.
     */
    public static BigDecimal overshoot(BigDecimal total, BigDecimal alreadyRecorded, BigDecimal amount) {
        BigDecimal collected = (alreadyRecorded == null ? BigDecimal.ZERO : alreadyRecorded).add(amount);
        BigDecimal excess = collected.subtract(total);
        return excess.signum() > 0 ? excess : BigDecimal.ZERO;
    }
}
