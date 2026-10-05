package com.hyperbrains.hms.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
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

    /**
     * The drug as it was when this line was prescribed, copied rather than read through {@link #drug}.
     *
     * <p>A catalogue entry can be renamed, repriced or reclassified; without these four the prescription would
     * afterwards describe a drug, a price and a class that were not what was prescribed — and it would do so
     * silently, because the line would still look internally consistent. Null on lines written before this was kept,
     * which is the honest value: the catalogue of today is not what it was then.
     */
    @Size(max = 255)
    @Column(name = "drug_name", length = 255)
    private String drugName;

    @Size(max = 50)
    @Column(name = "drug_unit", length = 50)
    private String drugUnit;

    @Column(name = "drug_price", precision = 21, scale = 2)
    private BigDecimal drugPrice;

    @Size(max = 50)
    @Column(name = "drug_classification", length = 50)
    private String drugClassification;

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

    public String getDrugName() {
        return this.drugName;
    }

    public void setDrugName(String drugName) {
        this.drugName = drugName;
    }

    public String getDrugUnit() {
        return this.drugUnit;
    }

    public void setDrugUnit(String drugUnit) {
        this.drugUnit = drugUnit;
    }

    public BigDecimal getDrugPrice() {
        return this.drugPrice;
    }

    public void setDrugPrice(BigDecimal drugPrice) {
        this.drugPrice = drugPrice;
    }

    public String getDrugClassification() {
        return this.drugClassification;
    }

    public void setDrugClassification(String drugClassification) {
        this.drugClassification = drugClassification;
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
