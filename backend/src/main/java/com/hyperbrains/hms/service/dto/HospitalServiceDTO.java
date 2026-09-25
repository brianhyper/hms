package com.hyperbrains.hms.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * A DTO for the {@link com.hyperbrains.hms.domain.HospitalService} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class HospitalServiceDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 160)
    private String name;

    @Size(max = 80)
    private String serviceType;

    @NotNull
    @DecimalMin(value = "0")
    private BigDecimal price;

    @NotNull
    private Boolean active;

    private String code;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

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

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
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
        if (!(o instanceof HospitalServiceDTO)) {
            return false;
        }

        HospitalServiceDTO hospitalServiceDTO = (HospitalServiceDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, hospitalServiceDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "HospitalServiceDTO{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", serviceType='" + getServiceType() + "'" +
            ", price=" + getPrice() +
            ", active='" + getActive() + "'" +
            "}";
    }
}
