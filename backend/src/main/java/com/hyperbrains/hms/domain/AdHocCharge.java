package com.hyperbrains.hms.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A AdHocCharge.
 */
@Entity
@Table(name = "ad_hoc_charge")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AdHocCharge implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Size(max = 500)
    @Column(name = "description", length = 500, nullable = false)
    private String description;

    @NotNull
    @DecimalMin(value = "0")
    @Column(name = "amount", precision = 21, scale = 2, nullable = false)
    private BigDecimal amount;

    @NotNull
    @Size(max = 10000)
    @Column(name = "reason", length = 10000, nullable = false)
    private String reason;

    @NotNull
    @Column(name = "added_at", nullable = false)
    private Instant addedAt;

    @Column(name = "voided_at")
    private Instant voidedAt;

    @Size(max = 10000)
    @Column(name = "void_reason", length = 10000)
    private String voidReason;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(
        value = { "visit", "bed", "admittingDoctor", "primaryDoctor", "dischargedByDoctor", "dischargedByNurse" },
        allowSetters = true
    )
    private Admission admission;

    @ManyToOne(fetch = FetchType.LAZY)
    private HospitalService serviceCatalogue;

    @ManyToOne(optional = false)
    @NotNull
    private User addedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    private User voidedBy;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public AdHocCharge id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return this.description;
    }

    public AdHocCharge description(String description) {
        this.setDescription(description);
        return this;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public AdHocCharge amount(BigDecimal amount) {
        this.setAmount(amount);
        return this;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getReason() {
        return this.reason;
    }

    public AdHocCharge reason(String reason) {
        this.setReason(reason);
        return this;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Instant getAddedAt() {
        return this.addedAt;
    }

    public AdHocCharge addedAt(Instant addedAt) {
        this.setAddedAt(addedAt);
        return this;
    }

    public void setAddedAt(Instant addedAt) {
        this.addedAt = addedAt;
    }

    public Instant getVoidedAt() {
        return this.voidedAt;
    }

    public AdHocCharge voidedAt(Instant voidedAt) {
        this.setVoidedAt(voidedAt);
        return this;
    }

    public void setVoidedAt(Instant voidedAt) {
        this.voidedAt = voidedAt;
    }

    public String getVoidReason() {
        return this.voidReason;
    }

    public AdHocCharge voidReason(String voidReason) {
        this.setVoidReason(voidReason);
        return this;
    }

    public void setVoidReason(String voidReason) {
        this.voidReason = voidReason;
    }

    public Admission getAdmission() {
        return this.admission;
    }

    public void setAdmission(Admission admission) {
        this.admission = admission;
    }

    public AdHocCharge admission(Admission admission) {
        this.setAdmission(admission);
        return this;
    }

    public HospitalService getServiceCatalogue() {
        return this.serviceCatalogue;
    }

    public void setServiceCatalogue(HospitalService hospitalService) {
        this.serviceCatalogue = hospitalService;
    }

    public AdHocCharge serviceCatalogue(HospitalService hospitalService) {
        this.setServiceCatalogue(hospitalService);
        return this;
    }

    public User getAddedBy() {
        return this.addedBy;
    }

    public void setAddedBy(User user) {
        this.addedBy = user;
    }

    public AdHocCharge addedBy(User user) {
        this.setAddedBy(user);
        return this;
    }

    public User getVoidedBy() {
        return this.voidedBy;
    }

    public void setVoidedBy(User user) {
        this.voidedBy = user;
    }

    public AdHocCharge voidedBy(User user) {
        this.setVoidedBy(user);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AdHocCharge)) {
            return false;
        }
        return getId() != null && getId().equals(((AdHocCharge) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AdHocCharge{" +
            "id=" + getId() +
            ", description='" + getDescription() + "'" +
            ", amount=" + getAmount() +
            ", reason='" + getReason() + "'" +
            ", addedAt='" + getAddedAt() + "'" +
            ", voidedAt='" + getVoidedAt() + "'" +
            ", voidReason='" + getVoidReason() + "'" +
            "}";
    }
}
