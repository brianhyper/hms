package com.hyperbrains.hms.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Actual item dispensed. The drug may differ from PrescriptionLine.drug when
 * an authorized substitution occurs.
 */
@Entity
@Table(name = "dispense_line")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class DispenseLine implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Min(value = 1)
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    @Column(name = "substitution_reason")
    private String substitutionReason;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "prescription", "recordedBy" }, allowSetters = true)
    private Dispense dispense;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "prescription", "drug" }, allowSetters = true)
    private PrescriptionLine prescriptionLine;

    @ManyToOne(optional = false)
    @NotNull
    private Drug drug;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public DispenseLine id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getQuantity() {
        return this.quantity;
    }

    public DispenseLine quantity(Integer quantity) {
        this.setQuantity(quantity);
        return this;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getSubstitutionReason() {
        return this.substitutionReason;
    }

    public DispenseLine substitutionReason(String substitutionReason) {
        this.setSubstitutionReason(substitutionReason);
        return this;
    }

    public void setSubstitutionReason(String substitutionReason) {
        this.substitutionReason = substitutionReason;
    }

    public Dispense getDispense() {
        return this.dispense;
    }

    public void setDispense(Dispense dispense) {
        this.dispense = dispense;
    }

    public DispenseLine dispense(Dispense dispense) {
        this.setDispense(dispense);
        return this;
    }

    public PrescriptionLine getPrescriptionLine() {
        return this.prescriptionLine;
    }

    public void setPrescriptionLine(PrescriptionLine prescriptionLine) {
        this.prescriptionLine = prescriptionLine;
    }

    public DispenseLine prescriptionLine(PrescriptionLine prescriptionLine) {
        this.setPrescriptionLine(prescriptionLine);
        return this;
    }

    public Drug getDrug() {
        return this.drug;
    }

    public void setDrug(Drug drug) {
        this.drug = drug;
    }

    public DispenseLine drug(Drug drug) {
        this.setDrug(drug);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DispenseLine)) {
            return false;
        }
        return getId() != null && getId().equals(((DispenseLine) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "DispenseLine{" +
            "id=" + getId() +
            ", quantity=" + getQuantity() +
            ", substitutionReason='" + getSubstitutionReason() + "'" +
            "}";
    }
}
