package com.hyperbrains.hms.domain;

import com.hyperbrains.hms.domain.enumeration.DrugClassification;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Drug.
 */
@Entity
@Table(name = "drug")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Drug implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Size(max = 200)
    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @NotNull
    @Size(max = 80)
    @Column(name = "unit", length = 80, nullable = false)
    private String unit;

    @NotNull
    @Min(value = 0)
    @Column(name = "current_stock", nullable = false)
    private Integer currentStock;

    @NotNull
    @Min(value = 0)
    @Column(name = "reserved_stock", nullable = false)
    private Integer reservedStock;

    @NotNull
    @Min(value = 0)
    @Column(name = "low_stock_threshold", nullable = false)
    private Integer lowStockThreshold;

    @NotNull
    @DecimalMin(value = "0")
    @Column(name = "price", precision = 21, scale = 2, nullable = false)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "classification")
    private DrugClassification classification;

    @NotNull
    @Column(name = "active", nullable = false)
    private Boolean active;

    /**
     * Optimistic locking guard. This is the one that actually matters for stock safety:
     * reservation is a read-modify-write on currentStock/reservedStock, and two nurses
     * prescribing the same drug at the same moment must not lose an update.
     */
    @Version
    @Column(name = "version")
    private int version;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Drug id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public Drug name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUnit() {
        return this.unit;
    }

    public Drug unit(String unit) {
        this.setUnit(unit);
        return this;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Integer getCurrentStock() {
        return this.currentStock;
    }

    public Drug currentStock(Integer currentStock) {
        this.setCurrentStock(currentStock);
        return this;
    }

    public void setCurrentStock(Integer currentStock) {
        this.currentStock = currentStock;
    }

    public Integer getReservedStock() {
        return this.reservedStock;
    }

    public Drug reservedStock(Integer reservedStock) {
        this.setReservedStock(reservedStock);
        return this;
    }

    public void setReservedStock(Integer reservedStock) {
        this.reservedStock = reservedStock;
    }

    public Integer getLowStockThreshold() {
        return this.lowStockThreshold;
    }

    public Drug lowStockThreshold(Integer lowStockThreshold) {
        this.setLowStockThreshold(lowStockThreshold);
        return this;
    }

    public void setLowStockThreshold(Integer lowStockThreshold) {
        this.lowStockThreshold = lowStockThreshold;
    }

    public BigDecimal getPrice() {
        return this.price;
    }

    public Drug price(BigDecimal price) {
        this.setPrice(price);
        return this;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public DrugClassification getClassification() {
        return this.classification;
    }

    public Drug classification(DrugClassification classification) {
        this.setClassification(classification);
        return this;
    }

    public void setClassification(DrugClassification classification) {
        this.classification = classification;
    }

    public Boolean getActive() {
        return this.active;
    }

    public Drug active(Boolean active) {
        this.setActive(active);
        return this;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public int getVersion() {
        return this.version;
    }

    public Drug version(int version) {
        this.setVersion(version);
        return this;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Drug)) {
            return false;
        }
        return getId() != null && getId().equals(((Drug) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Drug{" +
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
