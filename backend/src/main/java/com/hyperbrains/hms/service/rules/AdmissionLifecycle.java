package com.hyperbrains.hms.service.rules;

import com.hyperbrains.hms.domain.enumeration.AdmissionStatus;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * The rules that govern an admission's status.
 *
 * <p>Pure, so the legal moves can be tested one pair at a time rather than being inferred from whichever
 * transitions a test happened to push through the API.
 *
 * <p>The distinction this exists to protect is the one the specification calls out twice:
 * {@code VisitStatus.ADMITTED} means "this encounter is now an inpatient one" and is set the moment the
 * doctor converts it, while {@code AdmissionStatus.ADMITTED} means "this patient has a bed". They are
 * different moments — patients wait for beds — and {@link #awaitsABed} is the fact that separates them.
 */
public final class AdmissionLifecycle {

    private AdmissionLifecycle() {}

    /**
     * The moves that are legal, whoever performs them.
     *
     * <p>{@code PENDING_BED → DISCHARGED} is here because a patient who is admitted and then has to go
     * home — or dies, or leaves against advice — before a bed is ever found is a real sequence, and the
     * alternative is leaving a stay open forever with no bed and no patient.
     *
     * <p>{@code DISCHARGED} is terminal. Re-admitting somebody is a new admission on a new visit, not the
     * re-opening of this record: the dates on a stay are what the bed-days were billed from.
     */
    private static final Map<AdmissionStatus, Set<AdmissionStatus>> TRANSITIONS = Map.of(
        AdmissionStatus.PENDING_BED,
        EnumSet.of(AdmissionStatus.ADMITTED, AdmissionStatus.DISCHARGED),
        AdmissionStatus.ADMITTED,
        EnumSet.of(AdmissionStatus.DISCHARGED),
        AdmissionStatus.DISCHARGED,
        EnumSet.noneOf(AdmissionStatus.class)
    );

    /** The status a stay starts in: the decision has been made, no bed has been found yet. */
    public static AdmissionStatus statusOnAdmission() {
        return AdmissionStatus.PENDING_BED;
    }

    /** True while the patient is still waiting for a bed. */
    public static boolean awaitsABed(AdmissionStatus status) {
        return status == AdmissionStatus.PENDING_BED;
    }

    /** True once the patient is in a bed. */
    public static boolean holdsABed(AdmissionStatus status) {
        return status == AdmissionStatus.ADMITTED;
    }

    /**
     * True while the stay is running.
     *
     * <p>Used for "this patient is already an inpatient" — the check behind one open admission per
     * patient, which no database index can express because the patient is reached through the visit.
     */
    public static boolean isOpen(AdmissionStatus status) {
        return status != AdmissionStatus.DISCHARGED;
    }

    /** The status a stay reaches when a bed is given to it. */
    public static AdmissionStatus statusAfterBedAssigned() {
        return AdmissionStatus.ADMITTED;
    }

    /** Whether the move from one status to another is one the system allows at all. */
    public static boolean canTransition(AdmissionStatus from, AdmissionStatus to) {
        if (from == null || to == null) {
            return false;
        }
        return TRANSITIONS.getOrDefault(from, Set.of()).contains(to);
    }

    /** Every status a stay in this status may legally move to. */
    public static Set<AdmissionStatus> allowedTransitionsFrom(AdmissionStatus from) {
        if (from == null) {
            return Set.of();
        }
        return Set.copyOf(TRANSITIONS.getOrDefault(from, Set.of()));
    }
}
