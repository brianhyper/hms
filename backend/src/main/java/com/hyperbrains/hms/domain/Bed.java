package com.hyperbrains.hms.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hyperbrains.hms.domain.enumeration.BedStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Bed.
 */
@Entity
@Table(name = "bed")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Bed implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Size(max = 32)
    @Column(name = "bed_number", length = 32, nullable = false)
    private String bedNumber;

    /**
     * Optional per-bed price for a premium room; otherwise the bed type's rate
     * applies. Read when the bed-day charge is raised, so a rate change is never
     * retrospective.
     */
    @DecimalMin(value = "0")
    @Column(name = "daily_rate_override", precision = 21, scale = 2)
    private BigDecimal dailyRateOverride;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BedStatus status;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "department" }, allowSetters = true)
    private Ward ward;

    @ManyToOne(optional = false)
    @NotNull
    private BedType bedType;

    /**
     * Optimistic locking guard. Assigning a bed is the one place in Phase 2 where two people
     * genuinely race for the same row — two nurses putting two patients into the same free bed —
     * and a bed holding two patients is not a state anything can recover from. The column is added
     * by hand in 20260925140000_added_phase2_inpatient.xml, since @Version is not expressible in JDL.
     */
    @Version
    @Column(name = "version")
    private int version;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Bed id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBedNumber() {
        return this.bedNumber;
    }

    public Bed bedNumber(String bedNumber) {
        this.setBedNumber(bedNumber);
        return this;
    }

    public void setBedNumber(String bedNumber) {
        this.bedNumber = bedNumber;
    }

    public BigDecimal getDailyRateOverride() {
        return this.dailyRateOverride;
    }

    public Bed dailyRateOverride(BigDecimal dailyRateOverride) {
        this.setDailyRateOverride(dailyRateOverride);
        return this;
    }

    public void setDailyRateOverride(BigDecimal dailyRateOverride) {
        this.dailyRateOverride = dailyRateOverride;
    }

    public BedStatus getStatus() {
        return this.status;
    }

    public Bed status(BedStatus status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(BedStatus status) {
        this.status = status;
    }

    public Ward getWard() {
        return this.ward;
    }

    public void setWard(Ward ward) {
        this.ward = ward;
    }

    public Bed ward(Ward ward) {
        this.setWard(ward);
        return this;
    }

    public BedType getBedType() {
        return this.bedType;
    }

    public void setBedType(BedType bedType) {
        this.bedType = bedType;
    }

    public Bed bedType(BedType bedType) {
        this.setBedType(bedType);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Bed)) {
            return false;
        }
        return getId() != null && getId().equals(((Bed) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Bed{" +
            "id=" + getId() +
            ", bedNumber='" + getBedNumber() + "'" +
            ", dailyRateOverride=" + getDailyRateOverride() +
            ", status='" + getStatus() + "'" +
            "}";
    }
}
