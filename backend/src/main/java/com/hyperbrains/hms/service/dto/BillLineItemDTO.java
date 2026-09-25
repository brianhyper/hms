package com.hyperbrains.hms.service.dto;

import com.hyperbrains.hms.domain.enumeration.BillLineSourceType;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * A DTO for the {@link com.hyperbrains.hms.domain.BillLineItem} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class BillLineItemDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 255)
    private String description;

    @NotNull
    @DecimalMin(value = "0")
    private BigDecimal amount;

    @NotNull
    private BillLineSourceType sourceType;

    @NotNull
    private BillDTO bill;

    private String sourceRef;

    public String getSourceRef() {
        return sourceRef;
    }

    public void setSourceRef(String sourceRef) {
        this.sourceRef = sourceRef;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BillLineSourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(BillLineSourceType sourceType) {
        this.sourceType = sourceType;
    }

    public BillDTO getBill() {
        return bill;
    }

    public void setBill(BillDTO bill) {
        this.bill = bill;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BillLineItemDTO)) {
            return false;
        }

        BillLineItemDTO billLineItemDTO = (BillLineItemDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, billLineItemDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "BillLineItemDTO{" +
            "id=" + getId() +
            ", description='" + getDescription() + "'" +
            ", amount=" + getAmount() +
            ", sourceType='" + getSourceType() + "'" +
            ", bill=" + getBill() +
            "}";
    }
}
