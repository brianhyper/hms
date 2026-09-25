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
 * A dispensing transaction. Multiple transactions allow partial dispensing.
 */
@Entity
@Table(name = "dispense")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Dispense implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "dispensed_at", nullable = false)
    private Instant dispensedAt;
    @Column(name = "note")
    private String note;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "visit", "doctor" }, allowSetters = true)
    private Prescription prescription;

    @ManyToOne(optional = false)
    @NotNull
    private User recordedBy;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Dispense id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getDispensedAt() {
        return this.dispensedAt;
    }

    public Dispense dispensedAt(Instant dispensedAt) {
        this.setDispensedAt(dispensedAt);
        return this;
    }

    public void setDispensedAt(Instant dispensedAt) {
        this.dispensedAt = dispensedAt;
    }

    public String getNote() {
        return this.note;
    }

    public Dispense note(String note) {
        this.setNote(note);
        return this;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Prescription getPrescription() {
        return this.prescription;
    }

    public void setPrescription(Prescription prescription) {
        this.prescription = prescription;
    }

    public Dispense prescription(Prescription prescription) {
        this.setPrescription(prescription);
        return this;
    }

    public User getRecordedBy() {
        return this.recordedBy;
    }

    public void setRecordedBy(User user) {
        this.recordedBy = user;
    }

    public Dispense recordedBy(User user) {
        this.setRecordedBy(user);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Dispense)) {
            return false;
        }
        return getId() != null && getId().equals(((Dispense) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Dispense{" +
            "id=" + getId() +
            ", dispensedAt='" + getDispensedAt() + "'" +
            ", note='" + getNote() + "'" +
            "}";
    }
}
