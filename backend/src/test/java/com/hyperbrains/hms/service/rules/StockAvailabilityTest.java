package com.hyperbrains.hms.service.rules;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * The reservation arithmetic.
 *
 * <p>Every case here is a way the pharmacy can either promise medicine it does not have, or refuse
 * medicine it does. Both are real failures: the first bills a patient for something undispensable,
 * the second sends them away for a drug that is on the shelf.
 */
class StockAvailabilityTest {

    @Test
    void availableIsWhateverIsLeftAfterEveryPromise() {
        assertThat(StockAvailability.available(40, 12)).isEqualTo(28);
    }

    @Test
    void aFullyReservedDrugHasNothingAvailable() {
        assertThat(StockAvailability.available(10, 10)).isZero();
        assertThat(StockAvailability.canReserve(10, 10, 1)).isFalse();
    }

    /** Exactly enough is enough. An off-by-one here refuses a prescription that could have been filled. */
    @Test
    void reservingExactlyWhatIsAvailableIsAllowed() {
        assertThat(StockAvailability.canReserve(10, 4, 6)).isTrue();
        assertThat(StockAvailability.canReserve(10, 4, 7)).isFalse();
    }

    @Test
    void oneShortIsRefusedAndReportedAsOneShort() {
        assertThat(StockAvailability.canReserve(10, 4, 7)).isFalse();
        assertThat(StockAvailability.shortfall(10, 4, 7)).isEqualTo(1);
    }

    @Test
    void shortfallIsZeroWhenThereIsEnough() {
        assertThat(StockAvailability.shortfall(10, 4, 6)).isZero();
        assertThat(StockAvailability.shortfall(100, 0, 5)).isZero();
    }

    @Test
    void aReservationForNothingIsNotAReservation() {
        assertThat(StockAvailability.canReserve(100, 0, 0)).isFalse();
        assertThat(StockAvailability.canReserve(100, 0, -5)).isFalse();
    }

    @Test
    void aReservationLargerThanTheShelfIsRefusedAndTheShortfallIsTheRealGap() {
        assertThat(StockAvailability.canReserve(3, 0, 8)).isFalse();
        assertThat(StockAvailability.shortfall(3, 0, 8)).isEqualTo(5);
    }

    /**
     * The case that makes the reorder point meaningful: the shelf looks healthy, but almost all of it
     * is already promised, so the drug is about to run out.
     */
    @Test
    void theReorderPointCountsAvailabilityNotTheShelf() {
        assertThat(StockAvailability.belowThreshold(100, 98, 5)).isTrue();
        assertThat(StockAvailability.belowThreshold(100, 0, 5)).isFalse();
        // Exactly at the threshold is not yet below it.
        assertThat(StockAvailability.belowThreshold(10, 5, 5)).isFalse();
    }
}
