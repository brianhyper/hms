package com.hyperbrains.hms.service;

/**
 * Canonical values for {@code AuditLog.action}.
 *
 * <p>Kept in one place so the audit trail stays queryable. Free-text action names make it
 * impossible to answer "show me every correction to this patient" later, which is the whole point
 * of the table.
 */
public final class AuditActions {

    // Registration
    public static final String PATIENT_REGISTERED = "PATIENT_REGISTERED";
    public static final String PATIENT_EMERGENCY_INTAKE = "PATIENT_EMERGENCY_INTAKE";
    public static final String PATIENT_DUPLICATE_OVERRIDE = "PATIENT_DUPLICATE_OVERRIDE";
    public static final String PATIENT_CORRECTED = "PATIENT_CORRECTED";
    public static final String PATIENT_MERGED = "PATIENT_MERGED";

    // Triage
    public static final String VITALS_STARTED = "VITALS_STARTED";
    public static final String VITALS_RECORDED = "VITALS_RECORDED";
    public static final String VITALS_CORRECTED = "VITALS_CORRECTED";

    // Encounter
    public static final String APPOINTMENT_CHECKED_IN = "APPOINTMENT_CHECKED_IN";
    public static final String APPOINTMENT_CANCELLED = "APPOINTMENT_CANCELLED";
    public static final String APPOINTMENT_NO_SHOW = "APPOINTMENT_NO_SHOW";
    public static final String VISIT_CREATED = "VISIT_CREATED";
    public static final String VISIT_STATUS_CHANGED = "VISIT_STATUS_CHANGED";
    public static final String VISIT_QUEUE_SKIPPED = "VISIT_QUEUE_SKIPPED";
    public static final String CONSULTATION_STARTED = "CONSULTATION_STARTED";
    public static final String CONSULTATION_UPDATED = "CONSULTATION_UPDATED";
    public static final String CONSULTATION_COMPLETED = "CONSULTATION_COMPLETED";
    public static final String CONSULTATION_ADDENDUM = "CONSULTATION_ADDENDUM";
    public static final String PATIENT_ADMITTED = "PATIENT_ADMITTED";

    // Diagnostics
    public static final String ORDER_PLACED = "ORDER_PLACED";
    public static final String ORDER_RESULT_ENTERED = "ORDER_RESULT_ENTERED";
    public static final String ORDER_CANCELLED = "ORDER_CANCELLED";

    // Pharmacy
    public static final String PRESCRIPTION_PLACED = "PRESCRIPTION_PLACED";
    public static final String PRESCRIPTION_CANCELLED = "PRESCRIPTION_CANCELLED";
    public static final String STOCK_RESERVED = "STOCK_RESERVED";
    public static final String STOCK_RELEASED = "STOCK_RELEASED";
    public static final String STOCK_RECEIVED = "STOCK_RECEIVED";
    public static final String STOCK_WRITTEN_OFF = "STOCK_WRITTEN_OFF";
    public static final String STOCK_DISPENSED = "STOCK_DISPENSED";
    public static final String PRESCRIPTION_DISPENSED = "PRESCRIPTION_DISPENSED";
    public static final String PRESCRIPTION_READY_FOR_DISPENSE = "PRESCRIPTION_READY_FOR_DISPENSE";

    // Finance
    public static final String BILL_LINE_ADDED = "BILL_LINE_ADDED";
    public static final String BILL_LINE_VOIDED = "BILL_LINE_VOIDED";
    public static final String BILL_PAID = "BILL_PAID";
    public static final String PAYMENT_RECORDED = "PAYMENT_RECORDED";

    // Referral
    public static final String REFERRAL_CREATED = "REFERRAL_CREATED";
    public static final String REFERRAL_LETTER_EMAILED = "REFERRAL_LETTER_EMAILED";

    private AuditActions() {}
}
