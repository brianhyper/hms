package com.hyperbrains.hms.domain.enumeration;

/**
 * The PrescriptionStatus enumeration.
 */
public enum PrescriptionStatus {
    PENDING,
    READY_FOR_DISPENSE,
    PARTIALLY_DISPENSED,
    DISPENSED,
    PENDING_PAYMENT,
    PAID,
    /**
     * Withdrawn: the medicine will never be handed over.
     *
     * <p>Terminal. Only reachable before payment, because once money has been taken the medicine is
     * owed and undoing that is a refund rather than a cancellation.
     */
    CANCELLED,
}
