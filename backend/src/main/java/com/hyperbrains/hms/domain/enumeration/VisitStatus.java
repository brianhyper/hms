package com.hyperbrains.hms.domain.enumeration;

/**
 * The VisitStatus enumeration.
 */
public enum VisitStatus {
    REGISTERED,
    WAITING_VITALS,
    IN_VITALS,
    WAITING_DOCTOR,
    IN_CONSULTATION,
    WAITING_RESULTS,
    WAITING_PAYMENT,
    CLOSED,
    /**
     * An admitted patient's visit. The visit is NOT closed and NOT replaced when a doctor
     * uses admit-patient — its type changes to ADMISSION in place so that everything already
     * recorded (vitals, consultation, orders) stays attached. This status is the parking
     * state for that: the outpatient path toward payment is deliberately bypassed, and the
     * derivation must never move an ADMITTED visit to WAITING_PAYMENT or CLOSED. Inpatient
     * billing accumulates over the length of the stay under rules defined when the inpatient
     * domain is designed.
     */
    ADMITTED,
    CANCELLED,
}
