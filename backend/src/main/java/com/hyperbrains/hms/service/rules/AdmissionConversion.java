package com.hyperbrains.hms.service.rules;

import com.hyperbrains.hms.domain.enumeration.ConsultationStatus;
import com.hyperbrains.hms.domain.enumeration.VisitStatus;
import com.hyperbrains.hms.domain.enumeration.VisitType;
import java.util.Set;

/**
 * The rules for converting an outpatient visit into an admission.
 *
 * <p>The specification is emphatic about the shape of this: the visit is not closed and replaced by a
 * new one — its type changes in place, and everything already recorded against it (vitals, the
 * consultation, any orders already placed) stays attached. That makes this a change of <em>direction</em>
 * rather than the end of one encounter and the start of another, which is why nothing here closes,
 * cancels or re-creates anything.
 *
 * <p>Pure, so both gates — the state the visit is in, and whether a doctor has actually assessed the
 * patient — can be tested for every combination without a database.
 */
public final class AdmissionConversion {

    /**
     * Statuses a visit can no longer be converted from.
     *
     * <p>{@code CLOSED} and {@code CANCELLED} are the ends of the encounter: there is nothing left to
     * admit the patient <em>for</em>, and re-opening a finished visit would put charges back on a bill
     * that has already been settled. {@code ADMITTED} is listed as well so that a record whose type and
     * status disagree — which is to say corrupt data — cannot be "converted" a second time.
     */
    private static final Set<VisitStatus> NOT_CONVERTIBLE = Set.of(VisitStatus.CLOSED, VisitStatus.CANCELLED, VisitStatus.ADMITTED);

    private AdmissionConversion() {}

    /** True when this visit is already an admission. */
    public static boolean isAlreadyAdmitted(VisitType type) {
        return type == VisitType.ADMISSION;
    }

    /** Whether this visit may be converted to an admission at all. */
    public static boolean canConvert(VisitType type, VisitStatus status) {
        return !isAlreadyAdmitted(type) && !NOT_CONVERTIBLE.contains(status);
    }

    /**
     * Whether a doctor has seen this patient: the action is available "during or after a consultation".
     *
     * <p>Taking the nullable consultation status as the argument, rather than a boolean the caller has
     * already interpreted, keeps the meaning in one place. A patient with no consultation has not been
     * assessed by anyone, and {@code null} is the fact that says so.
     *
     * <p>This is the rule that refuses a pharmacy-only walk-in, which is the intended outcome: nobody
     * has examined the patient, so there is no clinical ground for keeping them in.
     */
    public static boolean hasBeenAssessed(ConsultationStatus consultationStatus) {
        return consultationStatus != null;
    }
}
