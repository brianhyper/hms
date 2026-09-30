package com.hyperbrains.hms.service.rules;

import com.hyperbrains.hms.domain.enumeration.DoctorOrderRecurrence;
import com.hyperbrains.hms.domain.enumeration.DoctorOrderStatus;
import com.hyperbrains.hms.domain.enumeration.DoctorOrderType;

/**
 * The rules that govern a doctor's order on the ward.
 *
 * <p>Pure, so the two decisions that matter can be tested without a database: who may end an order, and what
 * a single execution means.
 *
 * <p>The rule worth stating out loud is that a nurse does not decide when a course is over. Recording that a
 * dose was given completes a one-off instruction, because doing it is what the instruction asked for; it does
 * <em>not</em> complete a recurring one, because "the antibiotics are finished" is a clinical judgement the
 * prescriber makes, not a side effect of the last dose being charted.
 */
public final class DoctorOrderLifecycle {

    private DoctorOrderLifecycle() {}

    /** A newly placed order is running. There is no draft state: the ward works from what the doctor wrote. */
    public static DoctorOrderStatus statusOnPlacing() {
        return DoctorOrderStatus.ACTIVE;
    }

    /**
     * Whether a nurse may record carrying this order out.
     *
     * <p>Only while it is running. An order that has been stopped is not something to act on afterwards, and
     * a completed one is finished.
     */
    public static boolean isExecutable(DoctorOrderStatus status) {
        return status == DoctorOrderStatus.ACTIVE;
    }

    /** Whether the order is finished with, whatever finished it. */
    public static boolean isFinished(DoctorOrderStatus status) {
        return status != DoctorOrderStatus.ACTIVE;
    }

    /**
     * Whether one execution finishes the order.
     *
     * <p>True for a one-off instruction — the dose was given, the sample was taken, the instruction is done.
     * False for a recurring one, which stays running until the prescriber stops it: a course of antibiotics is
     * not over because somebody charted the last dose they were told about.
     */
    public static boolean completesOnExecution(DoctorOrderRecurrence recurrence) {
        return recurrence == DoctorOrderRecurrence.ONE_OFF;
    }

    /**
     * Whether placing this kind of order has to produce something else as well.
     *
     * <p>Only a drug order. A lab request is an instruction to take a sample and an instruction is just words;
     * medicine is stock, a pharmacy queue and money, none of which an order carries.
     */
    public static boolean requiresAPrescription(DoctorOrderType type) {
        return type == DoctorOrderType.DRUG;
    }

    /**
     * Whether this kind of order ever puts money on the bill.
     *
     * <p>An instruction never does. "Continue IV fluids" is a ward instruction, not a line on a bill, and
     * charging for it would be inventing a price for something the hospital never costed.
     */
    public static boolean isBillable(DoctorOrderType type) {
        return type != DoctorOrderType.INSTRUCTION;
    }
}
