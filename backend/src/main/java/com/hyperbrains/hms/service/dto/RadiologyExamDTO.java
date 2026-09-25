package com.hyperbrains.hms.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * A DTO for the {@link com.hyperbrains.hms.domain.RadiologyExam} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class RadiologyExamDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 160)
    private String name;

    @NotNull
    @DecimalMin(value = "0")
    private BigDecimal price;

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
        if (!(o instanceof RadiologyExamDTO)) {
            return false;
        }

        RadiologyExamDTO radiologyExamDTO = (RadiologyExamDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, radiologyExamDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "RadiologyExamDTO{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", price=" + getPrice() +
            ", active='" + getActive() + "'" +
            "}";
    }
}
