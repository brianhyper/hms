package com.hyperbrains.hms.service.dto;

import com.hyperbrains.hms.domain.enumeration.BedStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * A DTO for the {@link com.hyperbrains.hms.domain.Bed} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class BedDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 32)
    private String bedNumber;

    @DecimalMin(value = "0")
    @Schema(
        description = "Optional per-bed price for a premium room; otherwise the bed type's rate\napplies. Read when the bed-day charge is raised, so a rate change is never\nretrospective."
    )
    private BigDecimal dailyRateOverride;

    @NotNull
    private BedStatus status;

    @NotNull
    private WardDTO ward;

    @NotNull
    private BedTypeDTO bedType;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBedNumber() {
        return bedNumber;
    }

    public void setBedNumber(String bedNumber) {
        this.bedNumber = bedNumber;
    }

    public BigDecimal getDailyRateOverride() {
        return dailyRateOverride;
    }

    public void setDailyRateOverride(BigDecimal dailyRateOverride) {
        this.dailyRateOverride = dailyRateOverride;
    }

    public BedStatus getStatus() {
        return status;
    }

    public void setStatus(BedStatus status) {
        this.status = status;
    }

    public WardDTO getWard() {
        return ward;
    }

    public void setWard(WardDTO ward) {
        this.ward = ward;
    }

    public BedTypeDTO getBedType() {
        return bedType;
    }

    public void setBedType(BedTypeDTO bedType) {
        this.bedType = bedType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BedDTO)) {
            return false;
        }

        BedDTO bedDTO = (BedDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, bedDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "BedDTO{" +
            "id=" + getId() +
            ", bedNumber='" + getBedNumber() + "'" +
            ", dailyRateOverride=" + getDailyRateOverride() +
            ", status='" + getStatus() + "'" +
            ", ward=" + getWard() +
            ", bedType=" + getBedType() +
            "}";
    }
}
