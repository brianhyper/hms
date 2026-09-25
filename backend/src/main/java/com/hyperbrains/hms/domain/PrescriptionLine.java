package com.hyperbrains.hms.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A PrescriptionLine.
 */
@Entity
@Table(name = "prescription_line")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class PrescriptionLine implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Size(max = 200)
    @Column(name = "dosage", length = 200, nullable = false)
    private String dosage;

    @NotNull
    @Size(max = 100)
    @Column(name = "duration", length = 100, nullable = false)
    private String duration;

    @NotNull
    @Min(value = 1)
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "visit", "doctor" }, allowSetters = true)
    private Prescription prescription;

    @ManyToOne(optional = false)
    @NotNull
    private Drug drug;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public PrescriptionLine id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDosage() {
        return this.dosage;
    }

    public PrescriptionLine dosage(String dosage) {
        this.setDosage(dosage);
        return this;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getDuration() {
        return this.duration;
    }

    public PrescriptionLine duration(String duration) {
        this.setDuration(duration);
        return this;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public Integer getQuantity() {
        return this.quantity;
    }

    public PrescriptionLine quantity(Integer quantity) {
        this.setQuantity(quantity);
        return this;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Prescription getPrescription() {
        return this.prescription;
    }

    public void setPrescription(Prescription prescription) {
        this.prescription = prescription;
    }

    public PrescriptionLine prescription(Prescription prescription) {
        this.setPrescription(prescription);
        return this;
    }

    public Drug getDrug() {
        return this.drug;
    }

    public void setDrug(Drug drug) {
        this.drug = drug;
    }

    public PrescriptionLine drug(Drug drug) {
        this.setDrug(drug);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PrescriptionLine)) {
            return false;
        }
        return getId() != null && getId().equals(((PrescriptionLine) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "PrescriptionLine{" +
            "id=" + getId() +
            ", dosage='" + getDosage() + "'" +
            ", duration='" + getDuration() + "'" +
            ", quantity=" + getQuantity() +
            "}";
    }
}
