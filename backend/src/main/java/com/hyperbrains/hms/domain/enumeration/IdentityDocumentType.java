package com.hyperbrains.hms.domain.enumeration;

/**
 * The IdentityDocumentType enumeration.
 *
 * <p>{@link #PENDING} is not a kind of document. It is the explicit marker a registrar sets when a patient is old
 * enough to need a document but has not produced one yet: it says the identity was deliberately left open rather
 * than forgotten, which is what separates it from a blank field. It carries no number, and it is set only at
 * registration — a later correction records the real type and number and replaces it.
 */
public enum IdentityDocumentType {
    NATIONAL_ID,
    PASSPORT,
    BIRTH_CERTIFICATE,
    PENDING,
}
