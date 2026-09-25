package com.hyperbrains.hms.service.dto;

import com.hyperbrains.hms.domain.enumeration.PaymentPlanStatus;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.hyperbrains.hms.domain.PaymentPlan} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class PaymentPlanDTO implements Serializable {

    private Long id;

    @NotNull
    @DecimalMin(value = "0")
    private BigDecimal totalOwed;

    @NotNull
    private Instant agreedAt;

    @NotNull
    @Size(max = 200)
    private String guarantorName;

    @Size(max = 100)
    private String guarantorRelationship;

    @NotNull
    @Size(max = 32)
    private String guarantorPhone;

    @Size(max = 10000)
    private String notes;

    @NotNull
    private PaymentPlanStatus status;

    @NotNull
    private BillDTO bill;

    @NotNull
    private UserDTO agreedBy;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getTotalOwed() {
        return totalOwed;
    }

    public void setTotalOwed(BigDecimal totalOwed) {
        this.totalOwed = totalOwed;
    }

    public Instant getAgreedAt() {
        return agreedAt;
    }

    public void setAgreedAt(Instant agreedAt) {
        this.agreedAt = agreedAt;
    }

    public String getGuarantorName() {
        return guarantorName;
    }

    public void setGuarantorName(String guarantorName) {
        this.guarantorName = guarantorName;
    }

    public String getGuarantorRelationship() {
        return guarantorRelationship;
    }

    public void setGuarantorRelationship(String guarantorRelationship) {
        this.guarantorRelationship = guarantorRelationship;
    }

    public String getGuarantorPhone() {
        return guarantorPhone;
    }

    public void setGuarantorPhone(String guarantorPhone) {
        this.guarantorPhone = guarantorPhone;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public PaymentPlanStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentPlanStatus status) {
        this.status = status;
    }

    public BillDTO getBill() {
        return bill;
    }

    public void setBill(BillDTO bill) {
        this.bill = bill;
    }

    public UserDTO getAgreedBy() {
        return agreedBy;
    }

    public void setAgreedBy(UserDTO agreedBy) {
        this.agreedBy = agreedBy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PaymentPlanDTO)) {
            return false;
        }

        PaymentPlanDTO paymentPlanDTO = (PaymentPlanDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, paymentPlanDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "PaymentPlanDTO{" +
            "id=" + getId() +
            ", totalOwed=" + getTotalOwed() +
            ", agreedAt='" + getAgreedAt() + "'" +
            ", guarantorName='" + getGuarantorName() + "'" +
            ", guarantorRelationship='" + getGuarantorRelationship() + "'" +
            ", guarantorPhone='" + getGuarantorPhone() + "'" +
            ", notes='" + getNotes() + "'" +
            ", status='" + getStatus() + "'" +
            ", bill=" + getBill() +
            ", agreedBy=" + getAgreedBy() +
            "}";
    }
}
