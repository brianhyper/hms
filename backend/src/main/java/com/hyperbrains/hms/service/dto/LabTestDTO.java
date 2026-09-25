package com.hyperbrains.hms.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * A DTO for the {@link com.hyperbrains.hms.domain.LabTest} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class LabTestDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 160)
    private String name;

    @NotNull
    @DecimalMin(value = "0")
    private BigDecimal price;

    @Size(max = 100)
    private String specimenType;

    @Min(value = 0)
    private Integer turnaroundTimeMinutes;

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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getSpecimenType() {
        return specimenType;
    }

    public void setSpecimenType(String specimenType) {
        this.specimenType = specimenType;
    }

    public Integer getTurnaroundTimeMinutes() {
        return turnaroundTimeMinutes;
    }

    public void setTurnaroundTimeMinutes(Integer turnaroundTimeMinutes) {
        this.turnaroundTimeMinutes = turnaroundTimeMinutes;
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
        if (!(o instanceof LabTestDTO)) {
            return false;
        }

        LabTestDTO labTestDTO = (LabTestDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, labTestDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "LabTestDTO{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", price=" + getPrice() +
            ", specimenType='" + getSpecimenType() + "'" +
            ", turnaroundTimeMinutes=" + getTurnaroundTimeMinutes() +
            ", active='" + getActive() + "'" +
            "}";
    }
}
