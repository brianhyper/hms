package com.hyperbrains.hms.service.rules;

import com.hyperbrains.hms.domain.enumeration.VisitStatus;
import com.hyperbrains.hms.domain.enumeration.VisitType;

/**
 * Decides where a visit goes once a consultation finishes.
 *
 * <p>The rule the specification insists on is that this cannot be a linear status walk. A doctor
 * ordering a blood test <em>and</em> writing a prescription in the same consultation is routine, so
 * the visit is not "at" a stage — it is waiting on a <em>set</em> of things. This class therefore
 * takes a predicate over that set rather than a "current step", and {@code WAITING_RESULTS} is used
 * for "something is still outstanding", whatever the mix.
 *
 * <p>Pure, and deliberately tolerant of counts rather than entities, so the rule can be tested for
 * every combination of pending work without building a database.
 */
public final class VisitStatusDeriver {

    private VisitStatusDeriver() {}

    /** How much is still outstanding for a visit. */
    public record PendingWork(int openOrders, int unresolvedPrescriptions) {
        public static PendingWork none() {
            return new PendingWork(0, 0);
        }

        public boolean nothingOutstanding() {
            return openOrders == 0 && unresolvedPrescriptions == 0;
        }
    }

    /**
     * The status a visit should hold once a consultation completes.
     *
     * <p>An admitted patient is the exception: admission deliberately leaves the outpatient path, so
     * their visit parks in {@code ADMITTED} and never advances to payment however little is pending.
     * Their billing accumulates over the length of the stay under rules that are not part of this
     * phase.
     */
    public static VisitStatus afterConsultation(VisitType type, PendingWork pending) {
        if (type == VisitType.ADMISSION) {
            return VisitStatus.ADMITTED;
        }
        return pending.nothingOutstanding() ? VisitStatus.WAITING_PAYMENT : VisitStatus.WAITING_RESULTS;
    }

    /**
     * Whether this kind of visit takes part in the outpatient path toward payment at all.
     *
     * <p>Callers use this to avoid recomputing an admitted patient into a payment queue while
     * orders or prescriptions for their stay are still being resolved.
     */
    public static boolean participatesInOutpatientPath(VisitType type) {
        return type != VisitType.ADMISSION;
    }
}
