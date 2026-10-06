package com.hyperbrains.hms.service.rules;

import com.hyperbrains.hms.domain.enumeration.BillStatus;

/**
 * What has to be true before a stay can be discharged.
 *
 * <p>Phase 2 §7 asks for two things and neither is negotiable: **two sign-offs**, one from a doctor and one from a
 * nurse, each recording its own actor and its own audit entry; and a **billing gate**, because a patient who walks
 * out on an unsettled bill is the case the gate exists for. Both are pure questions about a row's columns, so they are
 * answered here rather than being inferred from whichever order a test happened to call the endpoints in.
 *
 * <p>The two sign-offs must be two people, and that is a refusal rather than a nicety. The specification's own
 * argument: one endpoint taking two names would let a single caller claim both, which is exactly what asking for two
 * is meant to prevent, and letting one person do both is "a decision to record, not an accident to allow". So this
 * refuses it, in one place, and the decision stays visible instead of depending on whether a caller happened to check.
 */
public final class DischargeRequirements {

    private DischargeRequirements() {}

    /**
     * Whether both sign-offs are in, from two different people.
     *
     * @param doctorSignOffUserId the doctor who signed, or null while they have not
     * @param nurseSignOffUserId the nurse who signed, or null while they have not
     */
    public static boolean isSignedOff(Long doctorSignOffUserId, Long nurseSignOffUserId) {
        if (doctorSignOffUserId == null || nurseSignOffUserId == null) {
            return false;
        }
        return !doctorSignOffUserId.equals(nurseSignOffUserId);
    }

    /** Whether one person is trying to be both halves of a discharge. */
    public static boolean isOnePersonSigningTwice(Long doctorSignOffUserId, Long nurseSignOffUserId) {
        return doctorSignOffUserId != null && doctorSignOffUserId.equals(nurseSignOffUserId);
    }

    /**
     * Whether the money allows a discharge to begin.
     *
     * <p>Three ways through, and the third is the one worth stating: the bill is settled, an arrangement to pay
     * covers it, or **there is no bill at all**. A stay that has incurred no charge has nothing outstanding, and
     * refusing over an absent bill would make a discharge impossible for precisely the stays that owe nothing —
     * including every stay admitted today, because the daily bed-day charge (Phase 2 slice 6) is not built yet.
     *
     * <p>An arrangement counts because the alternative is holding a patient in a bed until a guarantor's money
     * arrives, which is not a clinical decision and not one a hospital can take.
     */
    public static boolean moneyIsSettled(BillStatus billStatus, boolean coveredByAnActivePlan) {
        return billStatus == null || billStatus == BillStatus.PAID || coveredByAnActivePlan;
    }
}
