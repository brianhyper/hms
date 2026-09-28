package com.hyperbrains.hms.service.rules;

import com.hyperbrains.hms.domain.enumeration.BedStatus;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * The rules that govern a bed's status.
 *
 * <p>Pure, so the legal moves can be tested one pair of statuses at a time rather than being
 * inferred from whichever transitions a test happened to push through the API.
 *
 * <p>The cycle the specification describes is {@code OCCUPIED → CLEANING → AVAILABLE}, taken from
 * HL7 v2's bed status (housekeeping). The middle step is not decoration: a bed a patient has just
 * left is not ready for the next one, and a system that skips the step will quietly offer a dirty
 * bed to the next admission.
 */
public final class BedLifecycle {

    private BedLifecycle() {}

    /**
     * The moves that are legal, whoever or whatever performs them.
     *
     * <p>This is <em>not</em> the list of moves staff may make by hand. {@code OCCUPIED → CLEANING}
     * is performed by discharge, and {@code AVAILABLE → OCCUPIED} is not a status edit at all — it is
     * the consequence of assigning the bed to a patient, which is a separate action with its own
     * guards. The table answers "what can be true next", and the workflow services decide which of
     * those they expose.
     *
     * <p>{@code CLEANING → MAINTENANCE} is here because a cleaner who finds a broken bed has to be
     * able to take it out of service. Forcing that through {@code AVAILABLE} first would make the bed
     * offerable to the next patient in between.
     */
    private static final Map<BedStatus, Set<BedStatus>> TRANSITIONS = Map.of(
        BedStatus.AVAILABLE,
        EnumSet.of(BedStatus.MAINTENANCE),
        BedStatus.OCCUPIED,
        EnumSet.of(BedStatus.CLEANING),
        BedStatus.CLEANING,
        EnumSet.of(BedStatus.AVAILABLE, BedStatus.MAINTENANCE),
        BedStatus.MAINTENANCE,
        EnumSet.of(BedStatus.AVAILABLE)
    );

    /** The status a bed is left in when the patient in it leaves. */
    public static BedStatus statusAfterVacating() {
        return BedStatus.CLEANING;
    }

    /**
     * Whether this bed may be given to a patient.
     *
     * <p>Only {@code AVAILABLE} qualifies. A bed being cleaned, a bed under repair, and a bed still
     * holding somebody are all equally not-beds as far as admission is concerned, and treating them
     * differently is how two patients end up in one bed.
     */
    public static boolean isAssignable(BedStatus status) {
        return status == BedStatus.AVAILABLE;
    }

    /** Whether the bed currently holds a patient. */
    public static boolean isHoldingPatient(BedStatus status) {
        return status == BedStatus.OCCUPIED;
    }

    /**
     * Whether the move from one status to another is one the system allows at all.
     *
     * <p>A null on either side is not a transition, so an unset status cannot be "moved on" from.
     */
    public static boolean canTransition(BedStatus from, BedStatus to) {
        if (from == null || to == null) {
            return false;
        }
        return TRANSITIONS.getOrDefault(from, Set.of()).contains(to);
    }

    /** Every status a bed in this status may legally move to. */
    public static Set<BedStatus> allowedTransitionsFrom(BedStatus from) {
        if (from == null) {
            return Set.of();
        }
        return Set.copyOf(TRANSITIONS.getOrDefault(from, Set.of()));
    }
}
