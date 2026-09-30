package com.hyperbrains.hms.service.rules;

import java.time.Instant;

/**
 * The rules for a period of ward cover.
 *
 * <p>Cover is a window of time: a doctor is on duty for a ward from one moment until another, and the
 * second moment may be open-ended because nobody knows when the rotation ends. The access rule reads
 * this window, so the window is the thing worth testing directly rather than through a query — "is this
 * doctor covering this ward <em>now</em>" is a question with an off-by-one-second answer.
 *
 * <p>Pure, so both halves — whether a period makes sense at all, and whether it is in force at a given
 * moment — can be tested without a database.
 */
public final class WardCoverage {

    private WardCoverage() {}

    /**
     * Whether a cover period is well formed.
     *
     * <p>An open-ended period is fine; one that ends before it starts is not, and would make a doctor
     * invisible for the whole of the window they thought they had. Equal ends are refused too: a period
     * that covers no instant looks like cover on a roster and grants nothing.
     */
    public static boolean isWellFormed(Instant coversFrom, Instant coversTo) {
        if (coversFrom == null) {
            return false;
        }
        return coversTo == null || coversTo.isAfter(coversFrom);
    }

    /**
     * Whether this period is in force at a given moment.
     *
     * <p>The start is inclusive and the end exclusive: cover beginning at 08:00 is in force at 08:00, and
     * cover ending at 20:00 is not in force at 20:00. Handing over at the boundary is the case that makes
     * this matter — two doctors nominally covering one moment is harmless, no doctor covering it is not,
     * and the exclusive end is what stops the two windows overlapping.
     */
    public static boolean isActiveAt(Instant coversFrom, Instant coversTo, Instant at) {
        if (coversFrom == null || at == null) {
            return false;
        }
        if (at.isBefore(coversFrom)) {
            return false;
        }
        return coversTo == null || at.isBefore(coversTo);
    }

    /**
     * Whether this period grants anything at a given moment, for a ward that may or may not still be taking
     * patients.
     *
     * <p>The window is not the whole rule. Closing a ward is how a ward is taken out of service, and a roster
     * entry outlives the closure: it was a true arrangement when it was written, and nothing edits the roster
     * to say otherwise. Reading the window alone would leave a doctor holding access to the patients of a ward
     * the hospital has stopped using, so the closure belongs in the rule rather than being left to whichever
     * screens are expected to have caught up.
     */
    public static boolean isInForce(Instant coversFrom, Instant coversTo, Instant at, boolean wardTakesPatients) {
        return wardTakesPatients && isActiveAt(coversFrom, coversTo, at);
    }

    /** Whether this period has already ended by the given moment. */
    public static boolean hasEndedBy(Instant coversTo, Instant at) {
        return coversTo != null && at != null && !at.isBefore(coversTo);
    }
}
