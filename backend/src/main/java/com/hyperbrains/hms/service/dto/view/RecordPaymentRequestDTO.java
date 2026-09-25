package com.hyperbrains.hms.service.dto.view;

import com.hyperbrains.hms.domain.enumeration.PaymentConfirmationStatus;
import com.hyperbrains.hms.domain.enumeration.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Money arriving at the desk.
 *
 * <p>This is one payment event, not the running total: a counter that takes a deposit and then the
 * balance sends two of these, and the second is what settles the bill.
 */
public class RecordPaymentRequestDTO implements Serializable {

    @NotNull
    private PaymentMethod method;

    /**
     * How much was handed over now.
     *
     * <p>May be zero, but only when the bill owes nothing: settling a zero bill is a real act at a
     * desk (the patient has to be cleared to leave and their medicine released), and refusing it would
     * strand every visit whose charges came to nothing.
     */
    @NotNull
    @DecimalMin("0")
    private BigDecimal amount;

    /**
     * The receipt this payment was issued against.
     *
     * <p>Required whenever money changes hands, and unique. It is the only thing tying the hospital's
     * record to the patient's piece of paper, and it is what makes a retried submission recognisable
     * instead of a duplicate charge. Not required when nothing is collected, because there is nothing
     * to issue a receipt for.
     */
    @Size(max = 64)
    private String receiptNumber;

    /** Required when the method is MPESA, so the transaction can be reconciled against the statement. */
    @Size(max = 100)
    private String mpesaReference;

    /** Required when the method is INSURANCE. */
    @Size(max = 200)
    private String insurerName;

    /**
     * Whether the money is actually in hand.
     *
     * <p>Defaults to {@code CONFIRMED}, because a cashier recording a payment has the money. It matters
     * only for insurance, where a claim may be submitted long before it is honoured: a {@code PENDING}
     * payment is recorded as an attempt but does <em>not</em> settle the bill, and therefore does not
     * release medicine to the pharmacy.
     */
    private PaymentConfirmationStatus confirmationStatus;

    public PaymentMethod getMethod() {
        return method;
    }

    public void setMethod(PaymentMethod method) {
        this.method = method;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public void setReceiptNumber(String receiptNumber) {
        this.receiptNumber = receiptNumber;
    }

    public String getMpesaReference() {
        return mpesaReference;
    }

    public void setMpesaReference(String mpesaReference) {
        this.mpesaReference = mpesaReference;
    }

    public String getInsurerName() {
        return insurerName;
    }

    public void setInsurerName(String insurerName) {
        this.insurerName = insurerName;
    }

    public PaymentConfirmationStatus getConfirmationStatus() {
        return confirmationStatus;
    }

    public void setConfirmationStatus(PaymentConfirmationStatus confirmationStatus) {
        this.confirmationStatus = confirmationStatus;
    }
}
