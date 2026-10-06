package com.hyperbrains.hms.service.rules;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * The rules for a shift on the roster.
 *
 * <p>The successor to {@code WardCoverage}, moved onto the roster because the client's ruling 1a makes the roster
 * the single source of truth for who is on duty. The shape of the question changes with it, and that is the whole
 * reason this is a separate class rather than an edit: a cover was a window of two instants, and a shift is a day
 * plus two clock times, because the roster is read as a calendar.
 *
 * <p>Pure, so the awkward parts can be tested without a database. The awkward part is midnight: a night shift that
 * runs from 19:00 to 07:00 belongs to the day it started, so at 02:00 the person on duty is the one whose shift was
 * written for <em>yesterday</em>. {@link #candidateDates} exists so that "fetch the day and the day before" is a
 * decision this class makes and a test can pin, rather than a {@code minusDays(1)} that happens to be in a query.
 */
public final class ShiftDuty {

    private ShiftDuty() {}

    /**
     * Whether a shift is a window at all.
     *
     * <p>A shift that ends when it starts is refused rather than guessed at: it could mean nothing or the whole
     * day, and either reading is an invention. The roster says 07:00 to 19:00, or 19:00 to 07:00, and a 24-hour
     * duty is those two shifts.
     */
    public static boolean isWellFormed(LocalTime startsAt, LocalTime endsAt) {
        if (startsAt == null || endsAt == null) {
            return false;
        }
        return !startsAt.equals(endsAt);
    }

    /**
     * Whether the shift is on duty at a given moment, in the hospital's own wall clock.
     *
     * <p>The start is inclusive and the end exclusive, the same choice {@code WardCoverage} made: a handover at the
     * boundary is the case that matters, two people nominally on duty for one instant is harmless, and nobody being
     * on duty for it is not. An end before the start is not an error — it is the night shift, and it puts the shift
     * on duty from its own evening into the following morning.
     */
    public static boolean isOnDutyAt(LocalDate shiftDate, LocalTime startsAt, LocalTime endsAt, LocalDateTime at) {
        if (shiftDate == null || at == null || !isWellFormed(startsAt, endsAt)) {
            return false;
        }
        LocalDate date = at.toLocalDate();
        LocalTime time = at.toLocalTime();

        if (startsAt.isBefore(endsAt)) {
            return date.equals(shiftDate) && !time.isBefore(startsAt) && time.isBefore(endsAt);
        }
        // Past midnight: on duty from its own evening, and again in the small hours of the day after.
        return (date.equals(shiftDate) && !time.isBefore(startsAt)) || (date.equals(shiftDate.plusDays(1)) && time.isBefore(endsAt));
    }

    /**
     * The dates a roster query has to fetch to answer "who is on duty at this moment" honestly.
     *
     * <p>Today, and the day before — because of the night shift. A query that fetched only today would report nobody
     * on duty at 02:00, which is exactly when the question matters most.
     */
    public static List<LocalDate> candidateDates(LocalDateTime at) {
        LocalDate today = at.toLocalDate();
        return List.of(today, today.minusDays(1));
    }
}
