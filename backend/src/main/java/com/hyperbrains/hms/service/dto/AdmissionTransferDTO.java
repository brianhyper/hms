package com.hyperbrains.hms.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.hyperbrains.hms.domain.AdmissionTransfer} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AdmissionTransferDTO implements Serializable {

    private Long id;

    @NotNull
    private Instant transferredAt;

    @NotNull
    @Size(max = 10000)
    private String reason;

    @NotNull
    private AdmissionDTO admission;

    private BedDTO fromBed;

    @NotNull
    private BedDTO toBed;

    @NotNull
    private UserDTO transferredBy;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getTransferredAt() {
        return transferredAt;
    }

    public void setTransferredAt(Instant transferredAt) {
        this.transferredAt = transferredAt;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public AdmissionDTO getAdmission() {
        return admission;
    }

    public void setAdmission(AdmissionDTO admission) {
        this.admission = admission;
    }

    public BedDTO getFromBed() {
        return fromBed;
    }

    public void setFromBed(BedDTO fromBed) {
        this.fromBed = fromBed;
    }

    public BedDTO getToBed() {
        return toBed;
    }

    public void setToBed(BedDTO toBed) {
        this.toBed = toBed;
    }

    public UserDTO getTransferredBy() {
        return transferredBy;
    }

    public void setTransferredBy(UserDTO transferredBy) {
        this.transferredBy = transferredBy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AdmissionTransferDTO)) {
            return false;
        }

        AdmissionTransferDTO admissionTransferDTO = (AdmissionTransferDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, admissionTransferDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AdmissionTransferDTO{" +
            "id=" + getId() +
            ", transferredAt='" + getTransferredAt() + "'" +
            ", reason='" + getReason() + "'" +
            ", admission=" + getAdmission() +
            ", fromBed=" + getFromBed() +
            ", toBed=" + getToBed() +
            ", transferredBy=" + getTransferredBy() +
            "}";
    }
}
