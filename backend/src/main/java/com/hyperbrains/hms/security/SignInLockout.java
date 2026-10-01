package com.hyperbrains.hms.security;

import java.time.Duration;
import java.time.Instant;

/**
 * How long a sign-in lock keeps an account out.
 *
 * <p>The original rule was "no timer: an administrator releases it". That rule was made without the consequence on
 * the table — five wrong passwords lock any account whose login is known, nothing expired, and only a Super Admin
 * could release one at a time — so a script could lock every account in the hospital and the only way back was
 * somebody working through the list by hand. The lock therefore runs out on its own after a quarter of an hour, the
 * audit trail keeps the event, and the manual release stays for whoever cannot wait.
 *
 * <p>What that costs, stated here rather than discovered later: a window is a rate limit, not a bar. Five attempts
 * per fifteen minutes is roughly 480 guesses a day against one account, where "no timer" was five until a person
 * intervened. Once the lock is a window, the control that actually limits guessing is the password policy and
 * forcing the first-login change, not this.
 *
 * <p>Pure, and beside the filter rather than in {@code service/rules} for the same reason as {@link SessionIdle}:
 * the architecture test does not let the security layer read the service layer. Both the sign-in path and the
 * counting listener need one definition of when a lock is over — two copies would eventually disagree, and the
 * disagreement would be an account that is locked for the person signing in and open for the listener counting it.
 */
public final class SignInLockout {

    /** How long a lock keeps an account out before it runs out and the account is usable again. */
    public static final Duration LOCK_WINDOW = Duration.ofMinutes(15);

    private SignInLockout() {}

    /**
     * Whether a lock is still keeping sign-ins out.
     *
     * <p>Exactly the window counts as over: a lock set at 12:00 is in force until 12:15 and not at 12:15. The
     * boundary favours the person signing in, the same direction {@link SessionIdle} takes and for the same reason —
     * the other side of it is somebody who cannot get into the system at all.
     */
    public static boolean isInForce(Instant lockedAt, Instant now) {
        return lockedAt != null && now != null && now.isBefore(lockedAt.plus(LOCK_WINDOW));
    }

    /**
     * Whether a lock was set and has since run out.
     *
     * <p>The row still says "locked" until something clears it, and nothing runs on a timer: the next attempt at
     * signing in releases it, which is the only moment the answer matters and so the only moment the work is worth
     * doing. A null lock has not run out — it was never set.
     */
    public static boolean hasRunOut(Instant lockedAt, Instant now) {
        return lockedAt != null && now != null && !isInForce(lockedAt, now);
    }
}
