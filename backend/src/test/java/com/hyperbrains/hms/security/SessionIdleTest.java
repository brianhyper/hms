package com.hyperbrains.hms.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

/** Pure rules for an idle session. No database, no servlet, no clock of its own. */
class SessionIdleTest {

    private static final Instant NOW = Instant.parse("2026-09-30T12:00:00Z");

    @Test
    void aSessionThatHasJustBeenUsedIsNotIdle() {
        assertThat(SessionIdle.hasGoneIdle(NOW.minus(Duration.ofMinutes(29)), NOW.minus(Duration.ofHours(1)), NOW)).isFalse();
    }

    /** Which side of the boundary is a judgement call, so it is asserted rather than left to the reader. */
    @Test
    void exactlyTheWindowCountsAsIdle() {
        assertThat(SessionIdle.hasGoneIdle(NOW.minus(SessionIdle.TIMEOUT).plusSeconds(1), null, NOW)).isFalse();
        assertThat(SessionIdle.hasGoneIdle(NOW.minus(SessionIdle.TIMEOUT), null, NOW)).isTrue();
        assertThat(SessionIdle.hasGoneIdle(NOW.minus(SessionIdle.TIMEOUT).minusSeconds(1), null, NOW)).isTrue();
    }

    /**
     * The case the token's own issue time exists for. Without the floor, the token handed over at the desk after a
     * long absence would be refused on its first use, because the last activity recorded against the account is
     * from the session that timed out hours earlier.
     */
    @Test
    void aFreshlyIssuedTokenIsNotIdleWhateverWasRecordedBefore() {
        assertThat(SessionIdle.hasGoneIdle(NOW.minus(Duration.ofHours(4)), NOW, NOW)).isFalse();
        assertThat(SessionIdle.hasGoneIdle(NOW.minus(Duration.ofHours(4)), NOW.minus(Duration.ofMinutes(1)), NOW)).isFalse();
    }

    @Test
    void withNothingRecordedTheSessionIsAgedFromWhenItsTokenWasIssued() {
        assertThat(SessionIdle.hasGoneIdle(null, NOW.minus(Duration.ofMinutes(31)), NOW)).isTrue();
        assertThat(SessionIdle.hasGoneIdle(null, NOW.minus(Duration.ofMinutes(29)), NOW)).isFalse();
    }

    /** A gap in the data is not a reason to end somebody's session. */
    @Test
    void aSessionNothingCanBeSaidAboutIsNotIdle() {
        assertThat(SessionIdle.hasGoneIdle(null, null, NOW)).isFalse();
        assertThat(SessionIdle.hasGoneIdle(null, NOW, null)).isFalse();
        assertThat(SessionIdle.hasGoneIdle(NOW, NOW, null)).isFalse();
    }

    @Test
    void activityAfterTheTokenWasIssuedIsTheOneThatCounts() {
        assertThat(SessionIdle.hasGoneIdle(NOW.minus(Duration.ofMinutes(10)), NOW.minus(Duration.ofHours(2)), NOW)).isFalse();
    }

    @Test
    void activityIsRecordedWhenNothingHasBeenRecordedYet() {
        assertThat(SessionIdle.shouldRecordActivity(null, NOW)).isTrue();
    }

    /** A write per request would be a write per request to answer a question that changes every half hour. */
    @Test
    void activityIsRecordedAtMostOncePerInterval() {
        assertThat(SessionIdle.shouldRecordActivity(NOW.minusSeconds(29), NOW)).isFalse();
        assertThat(SessionIdle.shouldRecordActivity(NOW.minus(SessionIdle.WRITE_INTERVAL), NOW)).isTrue();
        assertThat(SessionIdle.shouldRecordActivity(NOW.minus(Duration.ofMinutes(5)), NOW)).isTrue();
        assertThat(SessionIdle.shouldRecordActivity(NOW, null)).as("no clock, no write").isFalse();
    }
}
