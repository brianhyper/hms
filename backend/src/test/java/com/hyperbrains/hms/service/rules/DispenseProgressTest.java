package com.hyperbrains.hms.service.rules;

import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.domain.enumeration.PrescriptionStatus;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * How the pharmacy's progress is derived from quantities actually handed over.
 *
 * <p>The mistake worth guarding against is marking a prescription DISPENSED when it is not: that takes
 * it off the queue and leaves the patient with medicine still owed and nobody asking for it.
 */
class DispenseProgressTest {

    @Test
    void remainingIsWhatHasNotBeenHandedOverYet() {
        assertThat(DispenseProgress.remaining(10, 4)).isEqualTo(6);
        assertThat(DispenseProgress.remaining(10, 0)).isEqualTo(10);
        assertThat(DispenseProgress.remaining(10, 10)).isZero();
    }

    @Test
    void remainingIsNeverNegative() {
        // More handed over than prescribed means something already went wrong; a negative remainder
        // would then read as "still outstanding" and re-queue medicine that has already left.
        assertThat(DispenseProgress.remaining(5, 7)).isZero();
    }

    @Test
    void askingForExactlyWhatIsLeftIsAllowed() {
        assertThat(DispenseProgress.exceedsRemaining(10, 4, 6)).isFalse();
        assertThat(DispenseProgress.exceedsRemaining(10, 4, 7)).isTrue();
    }

    @Test
    void askingForMoreThanWasEverPrescribedIsRefused() {
        assertThat(DispenseProgress.exceedsRemaining(3, 0, 4)).isTrue();
    }

    @Test
    void nothingLeftMeansDispensed() {
        assertThat(DispenseProgress.statusFor(List.of(0, 0, 0))).isEqualTo(PrescriptionStatus.DISPENSED);
    }

    @Test
    void anythingLeftMeansPartiallyDispensed() {
        assertThat(DispenseProgress.statusFor(List.of(0, 2))).isEqualTo(PrescriptionStatus.PARTIALLY_DISPENSED);
        assertThat(DispenseProgress.statusFor(List.of(5))).isEqualTo(PrescriptionStatus.PARTIALLY_DISPENSED);
    }

    /** Vacuously true, and the right answer: nothing outstanding means nothing left to hand over. */
    @Test
    void anEmptyPrescriptionIsDispensed() {
        assertThat(DispenseProgress.statusFor(List.of())).isEqualTo(PrescriptionStatus.DISPENSED);
    }
}
