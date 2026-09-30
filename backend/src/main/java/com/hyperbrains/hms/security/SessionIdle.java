package com.hyperbrains.hms.security;

import java.time.Duration;
import java.time.Instant;

/**
 * When a session has been left alone for too long.
 *
 * <p>Phase 3 fixes the window at thirty minutes: a session that has seen no request for that long is over, and the
 * next request on it is refused rather than quietly extending it.
 *
 * <p>Pure, so the boundaries — the idle moment itself, a token issued inside the window, and whether recording
 * activity is worth a write — can be decided without a database or a servlet.
 *
 * <p>It lives beside the filter rather than in {@code service/rules}, where this project's other pure rules are,
 * because the architecture test does not let the security layer read the service layer. Same kind of rule,
 * different home.
 */
public final class SessionIdle {

    /** How long a session may go unused. Fixed by Phase 3. */
    public static final Duration TIMEOUT = Duration.ofMinutes(30);

    /**
     * How stale the recorded activity may be before it is written again.
     *
     * <p>Every request in the hospital would otherwise mean a write, to answer a question whose answer only
     * changes every half hour. With this, a session costs at most one write per interval however busy it is.
     */
    public static final Duration WRITE_INTERVAL = Duration.ofSeconds(30);

    private SessionIdle() {}

    /**
     * Whether a session has gone idle.
     *
     * <p>The token's own issue time is a floor under the recorded activity, so signing in again after a long
     * absence starts a fresh session. Without that floor, the token handed over at the desk would be refused on
     * its first use, because the last activity recorded against the account would be from the session that timed
     * out hours earlier — a lockout delivered as a timeout.
     *
     * <p>Exactly the window counts as idle. Which side of the boundary that falls on is a judgement call, and
     * ending a session unused for precisely the window is the safer of the two readings.
     *
     * <p>A session nothing can be said about — no recorded activity and a token carrying no issue time — is not
     * called idle: refusing a session on the strength of a missing fact turns a gap in the data into a lockout.
     */
    public static boolean hasGoneIdle(Instant lastActivityAt, Instant issuedAt, Instant now) {
        if (now == null) {
            return false;
        }
        Instant since = latest(lastActivityAt, issuedAt);
        return since != null && !now.isBefore(since.plus(TIMEOUT));
    }

    /** Whether the recorded activity is old enough that recording it again is worth a write. */
    public static boolean shouldRecordActivity(Instant lastActivityAt, Instant now) {
        return now != null && (lastActivityAt == null || !now.isBefore(lastActivityAt.plus(WRITE_INTERVAL)));
    }

    private static Instant latest(Instant left, Instant right) {
        if (left == null) {
            return right;
        }
        if (right == null) {
            return left;
        }
        return left.isAfter(right) ? left : right;
    }
}
