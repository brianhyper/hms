package com.hyperbrains.hms.service.rules;

import com.hyperbrains.hms.domain.Visit;
import com.hyperbrains.hms.domain.enumeration.VisitPriority;
import com.hyperbrains.hms.domain.enumeration.VisitStatus;
import com.hyperbrains.hms.domain.enumeration.VisitType;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * The rules that govern how a visit is opened and the order staff work through a queue.
 *
 * <p>Pure, so the ordering and the initial-status decisions can be tested directly rather than
 * inferred from a database result.
 */
public final class VisitLifecycle {

    private VisitLifecycle() {}

    /**
     * The status a visit starts in, which depends on how it was opened.
     *
     * <p>A pharmacy-only visit has no triage and no consultation, so putting it in a vitals queue
     * would leave it stuck there forever with nobody to collect it. It waits for its prescription
     * instead, and moves on to payment when one is placed.
     */
    public static VisitStatus initialStatus(VisitType type) {
        return switch (type) {
            case PHARMACY_ONLY -> VisitStatus.REGISTERED;
            case OUTPATIENT, EMERGENCY -> VisitStatus.WAITING_VITALS;
            case ADMISSION -> throw new IllegalArgumentException(
                "A visit cannot be opened as an admission: admission is reached by converting an existing visit"
            );
        };
    }

    /**
     * Sort weight for a queue. Lower is seen first, so an emergency outranks a routine visit.
     *
     * <p>{@code VisitRepository.findQueue} duplicates these numbers in a CASE expression, because
     * the priority column holds a string and sorting on it directly would order alphabetically —
     * EMERGENCY, NORMAL, URGENT — quietly putting urgent patients last. The two must be changed
     * together, which is what the repository javadoc warns about.
     */
    public static int queueWeight(VisitPriority priority) {
        return switch (priority) {
            case EMERGENCY -> 0;
            case URGENT -> 1;
            case NORMAL -> 2;
        };
    }

    /** Priority band first, then longest-waiting first within the band. */
    public static Comparator<Visit> queueComparator() {
        return Comparator.comparingInt((Visit visit) -> queueWeight(visit.getPriority())).thenComparing(Visit::getCreatedAt);
    }

    /** Orders an in-memory queue the same way the database does. */
    public static List<Visit> sortQueue(List<Visit> queue) {
        return queue.stream().sorted(queueComparator()).toList();
    }

    /**
     * True when picking this visit means skipping past one that ranks ahead of it.
     *
     * <p>Picking out of order is allowed — staff often have a reason the system cannot see — but it
     * has to be recorded. This decides whether to demand that reason, so an out-of-order selection
     * is never silent and an in-order one is never nagged about.
     */
    public static boolean isOutOfOrder(Visit chosen, List<Visit> queueOrdered) {
        if (chosen == null || chosen.getId() == null || queueOrdered == null || queueOrdered.isEmpty()) {
            return false;
        }
        return !Objects.equals(queueOrdered.getFirst().getId(), chosen.getId());
    }

    /** A visit is finished with once it is closed, cancelled, or handed over to the inpatient side. */
    public static boolean isOpen(VisitStatus status) {
        return status != VisitStatus.CLOSED && status != VisitStatus.CANCELLED && status != VisitStatus.ADMITTED;
    }
}
