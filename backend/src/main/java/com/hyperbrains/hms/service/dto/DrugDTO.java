package com.hyperbrains.hms.service.dto;

import com.hyperbrains.hms.domain.enumeration.DrugClassification;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * A DTO for the {@link com.hyperbrains.hms.domain.Drug} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class DrugDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 200)
    private String name;

    @NotNull
    @Size(max = 80)
    private String unit;

    @NotNull
    @Min(value = 0)
    private Integer currentStock;

    @NotNull
    @Min(value = 0)
    private Integer reservedStock;

    @NotNull
    @Min(value = 0)
    private Integer lowStockThreshold;

    @NotNull
    @DecimalMin(value = "0")
    private BigDecimal price;

    private DrugClassification classification;

    @NotNull
    private Boolean active;

    private int version;

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
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

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Integer getCurrentStock() {
        return currentStock;
    }

    public void setCurrentStock(Integer currentStock) {
        this.currentStock = currentStock;
    }

    public Integer getReservedStock() {
        return reservedStock;
    }

    public void setReservedStock(Integer reservedStock) {
        this.reservedStock = reservedStock;
    }

    public Integer getLowStockThreshold() {
        return lowStockThreshold;
    }

    public void setLowStockThreshold(Integer lowStockThreshold) {
        this.lowStockThreshold = lowStockThreshold;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public DrugClassification getClassification() {
        return classification;
    }

    public void setClassification(DrugClassification classification) {
        this.classification = classification;
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
        if (!(o instanceof DrugDTO)) {
            return false;
        }

        DrugDTO drugDTO = (DrugDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, drugDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "DrugDTO{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", unit='" + getUnit() + "'" +
            ", currentStock=" + getCurrentStock() +
            ", reservedStock=" + getReservedStock() +
            ", lowStockThreshold=" + getLowStockThreshold() +
            ", price=" + getPrice() +
            ", classification='" + getClassification() + "'" +
            ", active='" + getActive() + "'" +
            "}";
    }
}
