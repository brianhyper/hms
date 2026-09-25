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

    /**
     * Whether a bill may be collected against a visit in this status.
     *
     * <p>Two situations qualify, and they are genuinely different situations:
     * <ul>
     *   <li>{@code WAITING_PAYMENT} — the outpatient desk, where the bill is finalised and paying ends
     *       the encounter;</li>
     *   <li>{@code ADMITTED} — an inpatient stay, whose bill runs for the whole stay and is collected
     *       against while the patient is still in the building. That is what a deposit or an instalment
     *       is.</li>
     * </ul>
     *
     * <p>Everything else is refused: before the payment stage the total is still moving as results come
     * back, and after {@code CLOSED} the money is already in.
     */
    public static boolean acceptsPayment(VisitStatus status) {
        return status == VisitStatus.WAITING_PAYMENT || status == VisitStatus.ADMITTED;
    }

    /**
     * Whether settling the bill is what ends the encounter.
     *
     * <p>True on the outpatient path. For an admission it is false by definition: the bill is collected
     * against for the length of the stay, so a payment is a payment and not a discharge — a stay ends
     * when a clinician says the patient may go home, which is not a financial question.
     *
     * <p>Deliberately a separate question from {@link VisitStatusDeriver#participatesInOutpatientPath},
     * not a duplicate of it: one asks whether a visit heads for the payment desk at all, the other
     * whether arriving there ends it. The two agree for every type today, and a test asserts that, so a
     * future visit type that splits them has to be a decision rather than an accident.
     */
    public static boolean closesOnBillSettlement(VisitType type) {
        return type != VisitType.ADMISSION;
    }
}
