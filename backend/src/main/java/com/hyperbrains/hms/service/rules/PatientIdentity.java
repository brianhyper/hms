package com.hyperbrains.hms.service.rules;

import com.hyperbrains.hms.domain.enumeration.IdentityDocumentType;
import java.time.LocalDate;
import java.time.Period;

/**
 * Whether a patient's identity document is required, and how old they are when that matters.
 *
 * <p>Past a certain age a patient is an adult, and an adult with no document on file is a file that cannot be matched
 * to a human being when it matters: at admission, in a duplicate review, or when a result has to be handed to the
 * right person. Below that age a document is often simply not held, so requiring one would refuse the children who
 * most need to be registered.
 *
 * <p>Pure, like the rest of this package: the decision is here and the refusal is in the service, so this can be
 * reasoned about and tested without Spring.
 */
public final class PatientIdentity {

    private PatientIdentity() {}

    /** The age above which a patient must have an identity document recorded. */
    public static final int DOCUMENT_REQUIRED_ABOVE_AGE = 19;

    /**
     * The patient's age in whole years, counted from the date of birth when one was recorded and taken from the
     * estimate otherwise — or {@code null} when neither is known.
     *
     * <p>An unknown age is not an adult age. The rule refuses adults without documents, and a patient whose age nobody
     * recorded is not someone to refuse at the desk; the missing document is pursued, the registration is not.
     */
    public static Integer ageInYears(LocalDate dateOfBirth, Integer estimatedAge, LocalDate today) {
        if (dateOfBirth != null) {
            return Period.between(dateOfBirth, today).getYears();
        }
        return estimatedAge;
    }

    /** Whether an age is old enough that a document is required. */
    public static boolean requiresDocument(Integer ageInYears) {
        return ageInYears != null && ageInYears > DOCUMENT_REQUIRED_ABOVE_AGE;
    }

    /** Whether a document number was actually supplied, rather than left blank. */
    public static boolean hasDocument(String documentNumber) {
        return documentNumber != null && !documentNumber.isBlank();
    }

    /** Whether the identity is the explicit pending marker rather than a real document. */
    public static boolean isPending(IdentityDocumentType type) {
        return type == IdentityDocumentType.PENDING;
    }

    /**
     * Whether the document type and the number contradict each other.
     *
     * <p>The pending marker is not a document and must carry no number; every other named type must carry one. A
     * blank type with a blank number is not a contradiction here — that is silence, and whether silence is refused
     * depends on the patient's age, which is a separate question.
     */
    public static boolean documentTypeAndNumberDisagree(IdentityDocumentType type, String documentNumber) {
        if (isPending(type)) {
            return hasDocument(documentNumber);
        }
        return type != null && !hasDocument(documentNumber);
    }

    /** Whether this patient, as described, is an adult who must have a document and does not have one. */
    public static boolean requiresDocumentButHasNone(LocalDate dateOfBirth, Integer estimatedAge, String documentNumber, LocalDate today) {
        return requiresDocument(ageInYears(dateOfBirth, estimatedAge, today)) && !hasDocument(documentNumber);
    }
}
