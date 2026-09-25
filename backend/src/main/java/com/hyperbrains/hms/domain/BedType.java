package com.hyperbrains.hms.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Bed categories are per-hospital data, not a fixed enum: ICU exists in one
 * hospital and not in the next. Super Admin managed, like Department.
 */
@Entity
@Table(name = "bed_type")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class BedType implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Size(max = 80)
    @Column(name = "name", length = 80, nullable = false, unique = true)
    private String name;

    @NotNull
    @DecimalMin(value = "0")
    @Column(name = "default_daily_rate", precision = 21, scale = 2, nullable = false)
    private BigDecimal defaultDailyRate;

    @NotNull
    @Column(name = "active", nullable = false)
    private Boolean active;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public BedType id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public BedType name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getDefaultDailyRate() {
        return this.defaultDailyRate;
    }

    public BedType defaultDailyRate(BigDecimal defaultDailyRate) {
        this.setDefaultDailyRate(defaultDailyRate);
        return this;
    }

    public void setDefaultDailyRate(BigDecimal defaultDailyRate) {
        this.defaultDailyRate = defaultDailyRate;
    }

    public Boolean getActive() {
        return this.active;
    }

    public BedType active(Boolean active) {
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
        if (!(o instanceof BedType)) {
            return false;
        }
        return getId() != null && getId().equals(((BedType) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "BedType{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", defaultDailyRate=" + getDefaultDailyRate() +
            ", active='" + getActive() + "'" +
            "}";
    }
}
