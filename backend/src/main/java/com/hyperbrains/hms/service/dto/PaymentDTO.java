package com.hyperbrains.hms.service.dto;

import com.hyperbrains.hms.domain.enumeration.PaymentConfirmationStatus;
import com.hyperbrains.hms.domain.enumeration.PaymentMethod;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.hyperbrains.hms.domain.Payment} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class PaymentDTO implements Serializable {

    private Long id;

    @NotNull
    private PaymentMethod method;

    @Size(max = 100)
    private String mpesaReference;

    @Size(max = 200)
    private String insurerName;

    @NotNull
    private PaymentConfirmationStatus confirmationStatus;

    @NotNull
    @Size(max = 64)
    private String receiptNumber;

    @NotNull
    @DecimalMin(value = "0")
    private BigDecimal amount;

    @NotNull
    private Instant recordedAt;

    @NotNull
    private UserDTO recordedBy;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public void setMethod(PaymentMethod method) {
        this.method = method;
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

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public void setReceiptNumber(String receiptNumber) {
        this.receiptNumber = receiptNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Instant getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(Instant recordedAt) {
        this.recordedAt = recordedAt;
    }

    public UserDTO getRecordedBy() {
        return recordedBy;
    }

    public void setRecordedBy(UserDTO recordedBy) {
        this.recordedBy = recordedBy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PaymentDTO)) {
            return false;
        }

        PaymentDTO paymentDTO = (PaymentDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, paymentDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "PaymentDTO{" +
            "id=" + getId() +
            ", method='" + getMethod() + "'" +
            ", mpesaReference='" + getMpesaReference() + "'" +
            ", insurerName='" + getInsurerName() + "'" +
            ", confirmationStatus='" + getConfirmationStatus() + "'" +
            ", receiptNumber='" + getReceiptNumber() + "'" +
            ", amount=" + getAmount() +
            ", recordedAt='" + getRecordedAt() + "'" +
            ", recordedBy=" + getRecordedBy() +
            "}";
    }
}
