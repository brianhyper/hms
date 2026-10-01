package com.hyperbrains.hms.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

/** Pure rules for how long a lock lasts. No database, no servlet, no clock of its own. */
class SignInLockoutTest {

    private static final Instant LOCKED_AT = Instant.parse("2026-10-01T12:00:00Z");

    @Test
    void aLockIsInForceUntilItsWindowIsUp() {
        assertThat(SignInLockout.isInForce(LOCKED_AT, LOCKED_AT)).as("in force the moment it goes on").isTrue();
        assertThat(SignInLockout.isInForce(LOCKED_AT, LOCKED_AT.plus(Duration.ofMinutes(14)))).isTrue();
        assertThat(SignInLockout.isInForce(LOCKED_AT, LOCKED_AT.plus(SignInLockout.LOCK_WINDOW).minusSeconds(1))).isTrue();
    }

    /**
     * The boundary favours the person signing in, which is asserted rather than left to the reader: the other side of
     * it is somebody who cannot get into the system because a clock has not ticked.
     */
    @Test
    void exactlyTheWindowCountsAsOver() {
        assertThat(SignInLockout.isInForce(LOCKED_AT, LOCKED_AT.plus(SignInLockout.LOCK_WINDOW))).isFalse();
        assertThat(SignInLockout.hasRunOut(LOCKED_AT, LOCKED_AT.plus(SignInLockout.LOCK_WINDOW))).isTrue();
        assertThat(SignInLockout.hasRunOut(LOCKED_AT, LOCKED_AT.plus(Duration.ofHours(3)))).isTrue();
    }

    @Test
    void aLockThatIsStillInForceHasNotRunOut() {
        assertThat(SignInLockout.hasRunOut(LOCKED_AT, LOCKED_AT.plus(Duration.ofMinutes(1)))).isFalse();
    }

    /** An account that was never locked has not "run out" — it must not be treated as a release either. */
    @Test
    void anAccountThatWasNeverLockedIsNeitherInForceNorRunOut() {
        assertThat(SignInLockout.isInForce(null, LOCKED_AT)).isFalse();
        assertThat(SignInLockout.hasRunOut(null, LOCKED_AT)).isFalse();
    }

    /** With no clock there is no answer, and neither "locked for ever" nor "released" is a safe guess. */
    @Test
    void withoutAClockNothingIsDecided() {
        assertThat(SignInLockout.isInForce(LOCKED_AT, null)).isFalse();
        assertThat(SignInLockout.hasRunOut(LOCKED_AT, null)).isFalse();
    }
}
