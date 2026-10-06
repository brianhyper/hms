package com.hyperbrains.hms.service.rules;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

/** Pure rules for a shift on the roster. No database involved. */
class ShiftDutyTest {

    private static final LocalDate THURSDAY = LocalDate.of(2026, 10, 8);

    private static final LocalTime DAY_START = LocalTime.of(7, 0);
    private static final LocalTime DAY_END = LocalTime.of(19, 0);

    private static final LocalTime NIGHT_START = LocalTime.of(19, 0);
    private static final LocalTime NIGHT_END = LocalTime.of(7, 0);

    @Test
    void aShiftMustRunFromAStartToADifferentEnd() {
        assertThat(ShiftDuty.isWellFormed(DAY_START, DAY_END)).isTrue();
        assertThat(ShiftDuty.isWellFormed(NIGHT_START, NIGHT_END)).as("past midnight is a real arrangement").isTrue();
    }

    /** Equal times could mean nothing or the whole day, and either reading is an invention. */
    @Test
    void aShiftThatEndsWhenItStartsIsRefusedRatherThanGuessedAt() {
        assertThat(ShiftDuty.isWellFormed(DAY_START, DAY_START)).isFalse();
        assertThat(ShiftDuty.isWellFormed(null, DAY_END)).isFalse();
        assertThat(ShiftDuty.isWellFormed(DAY_START, null)).isFalse();
    }

    /**
     * The start is inclusive and the end exclusive, so a handover at the boundary is one person's shift and not two:
     * two people nominally on duty for one instant is harmless, nobody being on duty for it is not.
     */
    @Test
    void aDayShiftIsOnDutyFromItsStartUntilItsEndNotIncludingIt() {
        assertThat(onDuty(THURSDAY, DAY_START, DAY_END, THURSDAY, LocalTime.of(6, 59))).isFalse();
        assertThat(onDuty(THURSDAY, DAY_START, DAY_END, THURSDAY, DAY_START)).as("in force when it starts").isTrue();
        assertThat(onDuty(THURSDAY, DAY_START, DAY_END, THURSDAY, LocalTime.of(18, 59))).isTrue();
        assertThat(onDuty(THURSDAY, DAY_START, DAY_END, THURSDAY, DAY_END)).as("handed over at the boundary").isFalse();
        assertThat(onDuty(THURSDAY, DAY_START, DAY_END, THURSDAY.plusDays(1), LocalTime.NOON))
            .as("and it is not still in force the next day")
            .isFalse();
    }

    /**
     * The case a day-plus-times roster exists for. A night shift belongs to the day it started, so at 02:00 the
     * person on duty is the one whose shift was written for yesterday — which is why the query fetches yesterday too.
     */
    @Test
    void aNightShiftIsStillOnDutyAfterMidnightOnTheDayItStarted() {
        assertThat(onDuty(THURSDAY, NIGHT_START, NIGHT_END, THURSDAY, LocalTime.of(18, 59))).isFalse();
        assertThat(onDuty(THURSDAY, NIGHT_START, NIGHT_END, THURSDAY, NIGHT_START)).isTrue();
        assertThat(onDuty(THURSDAY, NIGHT_START, NIGHT_END, THURSDAY, LocalTime.of(23, 59))).isTrue();
        assertThat(onDuty(THURSDAY, NIGHT_START, NIGHT_END, THURSDAY.plusDays(1), LocalTime.MIDNIGHT)).isTrue();
        assertThat(onDuty(THURSDAY, NIGHT_START, NIGHT_END, THURSDAY.plusDays(1), LocalTime.of(6, 59))).isTrue();
        assertThat(onDuty(THURSDAY, NIGHT_START, NIGHT_END, THURSDAY.plusDays(1), NIGHT_END))
            .as("handed over at 07:00")
            .isFalse();
        assertThat(onDuty(THURSDAY, NIGHT_START, NIGHT_END, THURSDAY.plusDays(1), LocalTime.NOON)).isFalse();
    }

    @Test
    void nonsenseIsOnDutyAtNoMoment() {
        assertThat(ShiftDuty.isOnDutyAt(null, DAY_START, DAY_END, LocalDateTime.of(THURSDAY, LocalTime.NOON))).isFalse();
        assertThat(ShiftDuty.isOnDutyAt(THURSDAY, DAY_START, DAY_END, null)).isFalse();
        assertThat(ShiftDuty.isOnDutyAt(THURSDAY, DAY_START, DAY_START, LocalDateTime.of(THURSDAY, LocalTime.NOON))).isFalse();
    }

    /** The dates the query has to fetch for a moment: its own day, and the one before it. */
    @Test
    void theCandidatesForAMomentAreItsDayAndTheOneBefore() {
        assertThat(ShiftDuty.candidateDates(LocalDateTime.of(THURSDAY, LocalTime.of(2, 0))))
            .as("02:00 needs yesterday, because that is when the night shift was written")
            .containsExactly(THURSDAY, THURSDAY.minusDays(1));
    }

    private static boolean onDuty(LocalDate shiftDate, LocalTime from, LocalTime to, LocalDate date, LocalTime time) {
        return ShiftDuty.isOnDutyAt(shiftDate, from, to, LocalDateTime.of(date, time));
    }
}
