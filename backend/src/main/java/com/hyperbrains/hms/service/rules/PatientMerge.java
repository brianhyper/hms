package com.hyperbrains.hms.service.rules;

import com.hyperbrains.hms.domain.enumeration.RegistrationStatus;

/**
 * Which records may be merged, and in which direction.
 *
 * <p>A merge is the one action here that combines two patient identities, so the question "which of these
 * is the real patient" is answered by the rule rather than by whoever calls it. The specification's case
 * is a temporary record adopting a confirmed one: the temporary record — created for a patient who could
 * not identify themselves — becomes a pointer to the real record, and everything clinical that happened
 * under the temporary identity follows the patient to the confirmed one.
 */
public final class PatientMerge {

    private PatientMerge() {}

    /** A record created without identifying details. This is the one that can be merged away. */
    public static boolean isTemporary(RegistrationStatus status) {
        return status == RegistrationStatus.INCOMPLETE_REGISTRATION;
    }

    /** A confirmed record. This is the one that survives, and the one whose hospital number is kept. */
    public static boolean isConfirmed(RegistrationStatus status) {
        return status == RegistrationStatus.COMPLETE;
    }

    /** Already merged away: it is a pointer now, not a patient, and nothing may be merged onto it. */
    public static boolean isAlreadyMerged(RegistrationStatus status) {
        return status == RegistrationStatus.MERGED;
    }

    /**
     * Whether this pair may be merged.
     *
     * <p>Merging two <em>confirmed</em> records is deliberately not allowed. Both are real patients with
     * their own hospital numbers and their own clinical history, so which identity should survive is a
     * judgement about a person, not something a rule can decide — and getting it wrong loses a real
     * patient's number. That case, if it is needed at all, has to be decided explicitly.
     */
    public static boolean isMergeable(RegistrationStatus source, RegistrationStatus target) {
        return isTemporary(source) && isConfirmed(target);
    }
}
