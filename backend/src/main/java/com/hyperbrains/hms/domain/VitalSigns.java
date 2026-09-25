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
 * A VitalSigns.
 */
@Entity
@Table(name = "vital_signs")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class VitalSigns implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @Column(name = "temperature", precision = 21, scale = 2)
    private BigDecimal temperature;

    @Column(name = "pulse_rate")
    private Integer pulseRate;

    @Column(name = "systolic_bp")
    private Integer systolicBp;

    @Column(name = "diastolic_bp")
    private Integer diastolicBp;

    @Column(name = "oxygen_saturation")
    private Integer oxygenSaturation;

    @Column(name = "weight", precision = 21, scale = 2)
    private BigDecimal weight;

    @Column(name = "height", precision = 21, scale = 2)
    private BigDecimal height;

    @Column(name = "bmi", precision = 21, scale = 2)
    private BigDecimal bmi;

    @Size(max = 100)
    @Column(name = "nutritional_status", length = 100)
    private String nutritionalStatus;

    @Column(name = "pregnancy_screening")
    private Boolean pregnancyScreening;
    @Column(name = "triage_notes")
    private String triageNotes;
    @Column(name = "other_measurements")
    private String otherMeasurements;

    @JsonIgnoreProperties(value = { "vitals", "consultation", "bill", "patient", "appointment" }, allowSetters = true)
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "vitals")
    private Visit visit;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public VitalSigns id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getTemperature() {
        return this.temperature;
    }

    public VitalSigns temperature(BigDecimal temperature) {
        this.setTemperature(temperature);
        return this;
    }

    public void setTemperature(BigDecimal temperature) {
        this.temperature = temperature;
    }

    public Integer getPulseRate() {
        return this.pulseRate;
    }

    public VitalSigns pulseRate(Integer pulseRate) {
        this.setPulseRate(pulseRate);
        return this;
    }

    public void setPulseRate(Integer pulseRate) {
        this.pulseRate = pulseRate;
    }

    public Integer getSystolicBp() {
        return this.systolicBp;
    }

    public VitalSigns systolicBp(Integer systolicBp) {
        this.setSystolicBp(systolicBp);
        return this;
    }

    public void setSystolicBp(Integer systolicBp) {
        this.systolicBp = systolicBp;
    }

    public Integer getDiastolicBp() {
        return this.diastolicBp;
    }

    public VitalSigns diastolicBp(Integer diastolicBp) {
        this.setDiastolicBp(diastolicBp);
        return this;
    }

    public void setDiastolicBp(Integer diastolicBp) {
        this.diastolicBp = diastolicBp;
    }

    public Integer getOxygenSaturation() {
        return this.oxygenSaturation;
    }

    public VitalSigns oxygenSaturation(Integer oxygenSaturation) {
        this.setOxygenSaturation(oxygenSaturation);
        return this;
    }

    public void setOxygenSaturation(Integer oxygenSaturation) {
        this.oxygenSaturation = oxygenSaturation;
    }

    public BigDecimal getWeight() {
        return this.weight;
    }

    public VitalSigns weight(BigDecimal weight) {
        this.setWeight(weight);
        return this;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public BigDecimal getHeight() {
        return this.height;
    }

    public VitalSigns height(BigDecimal height) {
        this.setHeight(height);
        return this;
    }

    public void setHeight(BigDecimal height) {
        this.height = height;
    }

    public BigDecimal getBmi() {
        return this.bmi;
    }

    public VitalSigns bmi(BigDecimal bmi) {
        this.setBmi(bmi);
        return this;
    }

    public void setBmi(BigDecimal bmi) {
        this.bmi = bmi;
    }

    public String getNutritionalStatus() {
        return this.nutritionalStatus;
    }

    public VitalSigns nutritionalStatus(String nutritionalStatus) {
        this.setNutritionalStatus(nutritionalStatus);
        return this;
    }

    public void setNutritionalStatus(String nutritionalStatus) {
        this.nutritionalStatus = nutritionalStatus;
    }

    public Boolean getPregnancyScreening() {
        return this.pregnancyScreening;
    }

    public VitalSigns pregnancyScreening(Boolean pregnancyScreening) {
        this.setPregnancyScreening(pregnancyScreening);
        return this;
    }

    public void setPregnancyScreening(Boolean pregnancyScreening) {
        this.pregnancyScreening = pregnancyScreening;
    }

    public String getTriageNotes() {
        return this.triageNotes;
    }

    public VitalSigns triageNotes(String triageNotes) {
        this.setTriageNotes(triageNotes);
        return this;
    }

    public void setTriageNotes(String triageNotes) {
        this.triageNotes = triageNotes;
    }

    public String getOtherMeasurements() {
        return this.otherMeasurements;
    }

    public VitalSigns otherMeasurements(String otherMeasurements) {
        this.setOtherMeasurements(otherMeasurements);
        return this;
    }

    public void setOtherMeasurements(String otherMeasurements) {
        this.otherMeasurements = otherMeasurements;
    }

    public Visit getVisit() {
        return this.visit;
    }

    public void setVisit(Visit visit) {
        if (this.visit != null) {
            this.visit.setVitals(null);
        }
        if (visit != null) {
            visit.setVitals(this);
        }
        this.visit = visit;
    }

    public VitalSigns visit(Visit visit) {
        this.setVisit(visit);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VitalSigns)) {
            return false;
        }
        return getId() != null && getId().equals(((VitalSigns) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "VitalSigns{" +
            "id=" + getId() +
            ", temperature=" + getTemperature() +
            ", pulseRate=" + getPulseRate() +
            ", systolicBp=" + getSystolicBp() +
            ", diastolicBp=" + getDiastolicBp() +
            ", oxygenSaturation=" + getOxygenSaturation() +
            ", weight=" + getWeight() +
            ", height=" + getHeight() +
            ", bmi=" + getBmi() +
            ", nutritionalStatus='" + getNutritionalStatus() + "'" +
            ", pregnancyScreening='" + getPregnancyScreening() + "'" +
            ", triageNotes='" + getTriageNotes() + "'" +
            ", otherMeasurements='" + getOtherMeasurements() + "'" +
            "}";
    }
}
