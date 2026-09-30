package com.hyperbrains.hms.service.rules;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;

/** Pure rules for a period of ward cover. No database involved. */
class WardCoverageTest {

    private static final Instant SHIFT_START = Instant.parse("2026-09-28T08:00:00Z");

    private static final Instant SHIFT_END = Instant.parse("2026-09-28T20:00:00Z");

    @Test
    void aPeriodMustEndAfterItStarts() {
        assertThat(WardCoverage.isWellFormed(SHIFT_START, SHIFT_END)).isTrue();
        assertThat(WardCoverage.isWellFormed(SHIFT_START, null)).as("open-ended is a real arrangement").isTrue();
    }

    /** A period that ends before it starts, or covers no instant at all, would look like cover and grant nothing. */
    @Test
    void aBackwardsOrEmptyPeriodIsNotWellFormed() {
        assertThat(WardCoverage.isWellFormed(SHIFT_START, SHIFT_START.minusSeconds(1))).isFalse();
        assertThat(WardCoverage.isWellFormed(SHIFT_START, SHIFT_START)).isFalse();
        assertThat(WardCoverage.isWellFormed(null, SHIFT_END)).isFalse();
        assertThat(WardCoverage.isWellFormed(null, null)).isFalse();
    }

    /**
     * The start is inclusive and the end exclusive, which is what makes a handover at the boundary work: two
     * doctors nominally covering one instant is harmless, nobody covering it is not.
     */
    @Test
    void coverIsInForceFromTheStartUntilTheEndNotIncludingIt() {
        assertThat(WardCoverage.isActiveAt(SHIFT_START, SHIFT_END, SHIFT_START)).as("in force when it starts").isTrue();
        assertThat(WardCoverage.isActiveAt(SHIFT_START, SHIFT_END, SHIFT_START.plusSeconds(1))).isTrue();
        assertThat(WardCoverage.isActiveAt(SHIFT_START, SHIFT_END, SHIFT_END.minusSeconds(1))).isTrue();
        assertThat(WardCoverage.isActiveAt(SHIFT_START, SHIFT_END, SHIFT_END)).as("no longer in force when it ends").isFalse();
    }

    @Test
    void coverOutsideItsWindowIsNotInForce() {
        assertThat(WardCoverage.isActiveAt(SHIFT_START, SHIFT_END, SHIFT_START.minusSeconds(1))).isFalse();
        assertThat(WardCoverage.isActiveAt(SHIFT_START, SHIFT_END, SHIFT_END.plusSeconds(1))).isFalse();
    }

    @Test
    void openEndedCoverStaysInForceUntilSomebodyEndsIt() {
        assertThat(WardCoverage.isActiveAt(SHIFT_START, null, SHIFT_START)).isTrue();
        assertThat(WardCoverage.isActiveAt(SHIFT_START, null, SHIFT_START.plusSeconds(86_400))).isTrue();
    }

    @Test
    void coverWithNoStartIsNeverInForceAndNothingIsInForceAtNoParticularTime() {
        assertThat(WardCoverage.isActiveAt(null, SHIFT_END, SHIFT_START)).isFalse();
        assertThat(WardCoverage.isActiveAt(SHIFT_START, SHIFT_END, null)).isFalse();
    }

    @Test
    void anEndedPeriodIsReportedAsEndedEvenWhenItIsAWindow() {
        assertThat(WardCoverage.hasEndedBy(SHIFT_END, SHIFT_END)).isTrue();
        assertThat(WardCoverage.hasEndedBy(SHIFT_END, SHIFT_END.minusSeconds(1))).isFalse();
        assertThat(WardCoverage.hasEndedBy(null, SHIFT_END)).as("open-ended has not ended").isFalse();
        assertThat(WardCoverage.hasEndedBy(SHIFT_END, null)).isFalse();
    }
}
