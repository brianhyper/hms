package com.hyperbrains.hms.service.rules;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/**
 * The arithmetic behind "has this bill been paid".
 *
 * <p>Small, but it is the arithmetic a patient's money depends on, and two of these cases are ways a
 * hospital either chases money it is not owed or accepts money it cannot account for.
 */
class BillSettlementTest {

    private static final BigDecimal TOTAL = new BigDecimal("1200.00");

    @Test
    void outstandingIsTheTotalLessWhatHasBeenCollected() {
        assertThat(BillSettlement.outstanding(TOTAL, new BigDecimal("500.00"))).isEqualByComparingTo("700.00");
        assertThat(BillSettlement.outstanding(TOTAL, BigDecimal.ZERO)).isEqualByComparingTo("1200.00");
    }

    @Test
    void aBillWithNothingRecordedOwesItsWholeTotal() {
        assertThat(BillSettlement.outstanding(TOTAL, null)).isEqualByComparingTo(TOTAL);
    }

    /**
     * A total reduced below what was collected is a refund situation. Reporting a negative debt would
     * have Finance chasing the patient for money the hospital owes them.
     */
    @Test
    void outstandingIsNeverNegative() {
        assertThat(BillSettlement.outstanding(new BigDecimal("100.00"), new BigDecimal("150.00"))).isEqualByComparingTo("0");
    }

    @Test
    void aPaymentThatCoversTheBalanceSettlesTheBill() {
        assertThat(BillSettlement.settles(TOTAL, new BigDecimal("500.00"), new BigDecimal("700.00"))).isTrue();
        assertThat(BillSettlement.settles(TOTAL, BigDecimal.ZERO, TOTAL)).isTrue();
    }

    @Test
    void aPaymentShortOfTheBalanceDoesNotSettleIt() {
        assertThat(BillSettlement.settles(TOTAL, BigDecimal.ZERO, new BigDecimal("1199.99"))).isFalse();
    }

    /**
     * Compared by value, not by {@code equals}: the request may carry {@code 1200.0} while the database
     * holds {@code 1200.00}, and BigDecimal.equals would call those different amounts of money.
     */
    @Test
    void settlementComparesAmountsByValueNotByScale() {
        assertThat(BillSettlement.settles(new BigDecimal("1200.00"), BigDecimal.ZERO, new BigDecimal("1200.0"))).isTrue();
        assertThat(BillSettlement.overshoot(new BigDecimal("1200.00"), BigDecimal.ZERO, new BigDecimal("1200.0000"))).isEqualByComparingTo(
            "0"
        );
    }

    @Test
    void aPaymentWithinTheBalanceOvershootsByNothing() {
        assertThat(BillSettlement.overshoot(TOTAL, BigDecimal.ZERO, new BigDecimal("1200.00"))).isEqualByComparingTo("0");
        assertThat(BillSettlement.overshoot(TOTAL, new BigDecimal("500.00"), new BigDecimal("700.00"))).isEqualByComparingTo("0");
    }

    @Test
    void aPaymentBeyondTheBalanceReportsTheExcess() {
        assertThat(BillSettlement.overshoot(TOTAL, BigDecimal.ZERO, new BigDecimal("1500.00"))).isEqualByComparingTo("300.00");
        assertThat(BillSettlement.overshoot(TOTAL, new BigDecimal("1100.00"), new BigDecimal("200.00"))).isEqualByComparingTo("100.00");
    }

    /** A bill with nothing owed settles against a payment of nothing, which is the subsidised case. */
    @Test
    void aBillWithNothingOwedSettlesAgainstNothing() {
        assertThat(BillSettlement.settles(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO)).isTrue();
        assertThat(BillSettlement.outstanding(BigDecimal.ZERO, BigDecimal.ZERO)).isEqualByComparingTo("0");
    }
}
