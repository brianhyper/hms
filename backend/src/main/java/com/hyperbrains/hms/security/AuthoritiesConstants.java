package com.hyperbrains.hms.security;

/**
 * Constants for Spring Security authorities.
 *
 * <p>{@link #ADMIN} / {@link #USER} are the stock JHipster roles. Everything below
 * {@link #SUPER_ADMIN} is a Phase 1 hospital role, seeded by
 * {@code config/liquibase/changelog/20260924120000_added_phase1_authorities.xml}.
 *
 * <p>The mapping from role to endpoint lives in exactly one place:
 * {@link com.hyperbrains.hms.config.SecurityConfiguration} (the "PHASE 1 RBAC TABLE"
 * block). Row-level and field-level restrictions (a Lab user only ever seeing lab
 * orders; Finance never seeing clinical notes) cannot be expressed as a role check and
 * are enforced in the service layer.
 */
public final class AuthoritiesConstants {

    public static final String ADMIN = "ROLE_ADMIN";

    public static final String USER = "ROLE_USER";

    public static final String ANONYMOUS = "ROLE_ANONYMOUS";

    // -----------------------------------------------------------------------------
    // Phase 1 hospital roles
    // -----------------------------------------------------------------------------

    /** Full administrative control, including user management and the audit trail. */
    public static final String SUPER_ADMIN = "ROLE_SUPER_ADMIN";

    /** Front desk: patient registration and appointment check-in. */
    public static final String RECEPTION = "ROLE_RECEPTION";

    /** Triage: records vital signs. */
    public static final String NURSE = "ROLE_NURSE";

    /** Consultations, orders, prescriptions, referrals, admission. */
    public static final String DOCTOR = "ROLE_DOCTOR";

    /** Laboratory queue: lab orders only. */
    public static final String LAB = "ROLE_LAB";

    /** Radiology queue: radiology orders only. */
    public static final String RADIOLOGY = "ROLE_RADIOLOGY";

    /** Dispensing queue and stock. */
    public static final String PHARMACY = "ROLE_PHARMACY";

    /** Billing and payment collection. */
    public static final String FINANCE = "ROLE_FINANCE";

    private AuthoritiesConstants() {}
}
