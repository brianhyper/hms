package com.hyperbrains.hms.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.hyperbrains.hms.domain.AdHocCharge} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AdHocChargeDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 500)
    private String description;

    @NotNull
    @DecimalMin(value = "0")
    private BigDecimal amount;

    @NotNull
    @Size(max = 10000)
    private String reason;

    @NotNull
    private Instant addedAt;

    private Instant voidedAt;

    @Size(max = 10000)
    private String voidReason;

    @NotNull
    private AdmissionDTO admission;

    private HospitalServiceDTO serviceCatalogue;

    @NotNull
    private UserDTO addedBy;

    private UserDTO voidedBy;

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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Instant getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(Instant addedAt) {
        this.addedAt = addedAt;
    }

    public Instant getVoidedAt() {
        return voidedAt;
    }

    public void setVoidedAt(Instant voidedAt) {
        this.voidedAt = voidedAt;
    }

    public String getVoidReason() {
        return voidReason;
    }

    public void setVoidReason(String voidReason) {
        this.voidReason = voidReason;
    }

    public AdmissionDTO getAdmission() {
        return admission;
    }

    public void setAdmission(AdmissionDTO admission) {
        this.admission = admission;
    }

    public HospitalServiceDTO getServiceCatalogue() {
        return serviceCatalogue;
    }

    public void setServiceCatalogue(HospitalServiceDTO serviceCatalogue) {
        this.serviceCatalogue = serviceCatalogue;
    }

    public UserDTO getAddedBy() {
        return addedBy;
    }

    public void setAddedBy(UserDTO addedBy) {
        this.addedBy = addedBy;
    }

    public UserDTO getVoidedBy() {
        return voidedBy;
    }

    public void setVoidedBy(UserDTO voidedBy) {
        this.voidedBy = voidedBy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AdHocChargeDTO)) {
            return false;
        }

        AdHocChargeDTO adHocChargeDTO = (AdHocChargeDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, adHocChargeDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AdHocChargeDTO{" +
            "id=" + getId() +
            ", description='" + getDescription() + "'" +
            ", amount=" + getAmount() +
            ", reason='" + getReason() + "'" +
            ", addedAt='" + getAddedAt() + "'" +
            ", voidedAt='" + getVoidedAt() + "'" +
            ", voidReason='" + getVoidReason() + "'" +
            ", admission=" + getAdmission() +
            ", serviceCatalogue=" + getServiceCatalogue() +
            ", addedBy=" + getAddedBy() +
            ", voidedBy=" + getVoidedBy() +
            "}";
    }
}
