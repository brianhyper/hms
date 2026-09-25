package com.hyperbrains.hms.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A AdmissionTransfer.
 */
@Entity
@Table(name = "admission_transfer")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AdmissionTransfer implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "transferred_at", nullable = false)
    private Instant transferredAt;

    @NotNull
    @Size(max = 10000)
    @Column(name = "reason", length = 10000, nullable = false)
    private String reason;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(
        value = { "visit", "bed", "admittingDoctor", "primaryDoctor", "dischargedByDoctor", "dischargedByNurse" },
        allowSetters = true
    )
    private Admission admission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "ward", "bedType" }, allowSetters = true)
    private Bed fromBed;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "ward", "bedType" }, allowSetters = true)
    private Bed toBed;

    @ManyToOne(optional = false)
    @NotNull
    private User transferredBy;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public AdmissionTransfer id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getTransferredAt() {
        return this.transferredAt;
    }

    public AdmissionTransfer transferredAt(Instant transferredAt) {
        this.setTransferredAt(transferredAt);
        return this;
    }

    public void setTransferredAt(Instant transferredAt) {
        this.transferredAt = transferredAt;
    }

    public String getReason() {
        return this.reason;
    }

    public AdmissionTransfer reason(String reason) {
        this.setReason(reason);
        return this;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Admission getAdmission() {
        return this.admission;
    }

    public void setAdmission(Admission admission) {
        this.admission = admission;
    }

    public AdmissionTransfer admission(Admission admission) {
        this.setAdmission(admission);
        return this;
    }

    public Bed getFromBed() {
        return this.fromBed;
    }

    public void setFromBed(Bed bed) {
        this.fromBed = bed;
    }

    public AdmissionTransfer fromBed(Bed bed) {
        this.setFromBed(bed);
        return this;
    }

    public Bed getToBed() {
        return this.toBed;
    }

    public void setToBed(Bed bed) {
        this.toBed = bed;
    }

    public AdmissionTransfer toBed(Bed bed) {
        this.setToBed(bed);
        return this;
    }

    public User getTransferredBy() {
        return this.transferredBy;
    }

    public void setTransferredBy(User user) {
        this.transferredBy = user;
    }

    public AdmissionTransfer transferredBy(User user) {
        this.setTransferredBy(user);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AdmissionTransfer)) {
            return false;
        }
        return getId() != null && getId().equals(((AdmissionTransfer) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AdmissionTransfer{" +
            "id=" + getId() +
            ", transferredAt='" + getTransferredAt() + "'" +
            ", reason='" + getReason() + "'" +
            "}";
    }
}
