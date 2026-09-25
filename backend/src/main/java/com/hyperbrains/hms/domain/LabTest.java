package com.hyperbrains.hms.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A LabTest.
 */
@Entity
@Table(name = "lab_test")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class LabTest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Size(max = 160)
    @Column(name = "name", length = 160, nullable = false)
    private String name;

    @NotNull
    @DecimalMin(value = "0")
    @Column(name = "price", precision = 21, scale = 2, nullable = false)
    private BigDecimal price;

    @Size(max = 100)
    @Column(name = "specimen_type", length = 100)
    private String specimenType;

    @Min(value = 0)
    @Column(name = "turnaround_time_minutes")
    private Integer turnaroundTimeMinutes;

    @NotNull
    @Column(name = "active", nullable = false)
    private Boolean active;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public LabTest id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public LabTest name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return this.price;
    }

    public LabTest price(BigDecimal price) {
        this.setPrice(price);
        return this;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getSpecimenType() {
        return this.specimenType;
    }

    public LabTest specimenType(String specimenType) {
        this.setSpecimenType(specimenType);
        return this;
    }

    public void setSpecimenType(String specimenType) {
        this.specimenType = specimenType;
    }

    public Integer getTurnaroundTimeMinutes() {
        return this.turnaroundTimeMinutes;
    }

    public LabTest turnaroundTimeMinutes(Integer turnaroundTimeMinutes) {
        this.setTurnaroundTimeMinutes(turnaroundTimeMinutes);
        return this;
    }

    public void setTurnaroundTimeMinutes(Integer turnaroundTimeMinutes) {
        this.turnaroundTimeMinutes = turnaroundTimeMinutes;
    }

    public Boolean getActive() {
        return this.active;
    }

    public LabTest active(Boolean active) {
        this.setActive(active);
        return this;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LabTest)) {
            return false;
        }
        return getId() != null && getId().equals(((LabTest) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "LabTest{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", price=" + getPrice() +
            ", specimenType='" + getSpecimenType() + "'" +
            ", turnaroundTimeMinutes=" + getTurnaroundTimeMinutes() +
            ", active='" + getActive() + "'" +
            "}";
    }
}
