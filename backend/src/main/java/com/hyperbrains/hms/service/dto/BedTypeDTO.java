package com.hyperbrains.hms.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * A DTO for the {@link com.hyperbrains.hms.domain.BedType} entity.
 */
@Schema(
    description = "Bed categories are per-hospital data, not a fixed enum: ICU exists in one\nhospital and not in the next. Super Admin managed, like Department."
)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class BedTypeDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 80)
    private String name;

    @NotNull
    @DecimalMin(value = "0")
    private BigDecimal defaultDailyRate;

    @NotNull
    private Boolean active;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getDefaultDailyRate() {
        return defaultDailyRate;
    }

    public void setDefaultDailyRate(BigDecimal defaultDailyRate) {
        this.defaultDailyRate = defaultDailyRate;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BedTypeDTO)) {
            return false;
        }

        BedTypeDTO bedTypeDTO = (BedTypeDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, bedTypeDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "BedTypeDTO{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", defaultDailyRate=" + getDefaultDailyRate() +
            ", active='" + getActive() + "'" +
            "}";
    }
}
