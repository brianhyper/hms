package com.hyperbrains.hms.service.rules;

import java.util.Set;

/**
 * Which parts of a patient record a correction may touch, and which of them are clinical.
 *
 * <p>Classification only — who is <em>allowed</em> to correct what is decided in the service, because
 * that answer needs the caller's authorities and this class is kept free of Spring so the tables can be
 * reasoned about and tested on their own.
 *
 * <p>The split matters more than it looks. A mistyped phone number is discovered at the registration
 * desk, so the desk must be able to fix it. A recorded allergy is a clinical fact, and a recorded
 * allergy that has been edited by whoever was nearest the keyboard is worse than one that is slightly
 * out of date.
 */
public final class PatientFields {

    private PatientFields() {}

    /** Identifying and contact details: the registration desk's business. */
    public static final Set<String> DEMOGRAPHIC = Set.of(
        "fullName",
        "dateOfBirth",
        "estimatedAge",
        "sex",
        "sexEstimated",
        "phone",
        "email",
        "identityDocumentType",
        "identityDocumentNumber",
        "occupation",
        "maritalStatus",
        "nextOfKinName",
        "nextOfKinPhone",
        "nextOfKinRelationship",
        "villageEstate"
    );

    /** Facts about the patient's body that a clinician recorded. */
    public static final Set<String> CLINICAL = Set.of("knownAllergies", "knownConditions");

    /**
     * Deliberately not correctable, whatever the role:
     * <ul>
     *   <li>{@code hospitalId} — the permanent identifier other records and printed letters refer to.
     *       Changing it would break every reference to this patient.</li>
     *   <li>{@code registrationStatus} and {@code mergedIntoPatient} — those move through merging,
     *       which is its own action with its own rules, not a field to be typed over.</li>
     * </ul>
     */
    public static final Set<String> NEVER_CORRECTABLE = Set.of("hospitalId", "registrationStatus", "mergedIntoPatient");

    public static boolean isKnown(String field) {
        return DEMOGRAPHIC.contains(field) || CLINICAL.contains(field);
    }

    public static boolean isClinical(String field) {
        return CLINICAL.contains(field);
    }
}
