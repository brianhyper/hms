package com.hyperbrains.hms.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hyperbrains.hms.domain.enumeration.PrescriptionSource;
import com.hyperbrains.hms.domain.enumeration.PrescriptionStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Prescription.
 */
@Entity
@Table(name = "prescription")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Prescription implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false)
    private PrescriptionSource source;

    @Size(max = 255)
    @Column(name = "prescribing_source", length = 255)
    private String prescribingSource;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PrescriptionStatus status;

    /** When it was written. Without this a dispensing queue cannot say how long a patient has waited. */
    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "vitals", "consultation", "bill", "patient", "appointment" }, allowSetters = true)
    private Visit visit;

    @ManyToOne(fetch = FetchType.LAZY)
    private User doctor;

    /**
     * Optimistic locking guard. Dispensing (pharmacy) and payment (finance) advance the
     * same state machine from two different stations.
     */
    @Version
    @Column(name = "version")
    private int version;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Prescription id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PrescriptionSource getSource() {
        return this.source;
    }

    public Prescription source(PrescriptionSource source) {
        this.setSource(source);
        return this;
    }

    public void setSource(PrescriptionSource source) {
        this.source = source;
    }

    public String getPrescribingSource() {
        return this.prescribingSource;
    }

    public Prescription prescribingSource(String prescribingSource) {
        this.setPrescribingSource(prescribingSource);
        return this;
    }

    public void setPrescribingSource(String prescribingSource) {
        this.prescribingSource = prescribingSource;
    }

    public PrescriptionStatus getStatus() {
        return this.status;
    }

    public Prescription status(PrescriptionStatus status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(PrescriptionStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public Prescription createdAt(Instant createdAt) {
        this.setCreatedAt(createdAt);
        return this;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Visit getVisit() {
        return this.visit;
    }

    public void setVisit(Visit visit) {
        this.visit = visit;
    }

    public Prescription visit(Visit visit) {
        this.setVisit(visit);
        return this;
    }

    public User getDoctor() {
        return this.doctor;
    }

    public void setDoctor(User user) {
        this.doctor = user;
    }

    public Prescription doctor(User user) {
        this.setDoctor(user);
        return this;
    }

    public int getVersion() {
        return this.version;
    }

    public Prescription version(int version) {
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
        if (!(o instanceof Prescription)) {
            return false;
        }
        return getId() != null && getId().equals(((Prescription) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Prescription{" +
            "id=" + getId() +
            ", source='" + getSource() + "'" +
            ", prescribingSource='" + getPrescribingSource() + "'" +
            ", status='" + getStatus() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            "}";
    }
}
