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
 * Inpatient observations. Separate from VitalSigns because that entity is unique
 * per visit — a correct shape for one triage moment and the wrong one for a
 * patient charted twice a day for a week.
 */
@Entity
@Table(name = "inpatient_vitals")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class InpatientVitals implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

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

    @Size(max = 10000)
    @Column(name = "notes", length = 10000)
    private String notes;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(
        value = { "visit", "bed", "admittingDoctor", "primaryDoctor", "dischargedByDoctor", "dischargedByNurse" },
        allowSetters = true
    )
    private Admission admission;

    @ManyToOne(optional = false)
    @NotNull
    private User recordedBy;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public InpatientVitals id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getRecordedAt() {
        return this.recordedAt;
    }

    public InpatientVitals recordedAt(Instant recordedAt) {
        this.setRecordedAt(recordedAt);
        return this;
    }

    public void setRecordedAt(Instant recordedAt) {
        this.recordedAt = recordedAt;
    }

    public BigDecimal getTemperature() {
        return this.temperature;
    }

    public InpatientVitals temperature(BigDecimal temperature) {
        this.setTemperature(temperature);
        return this;
    }

    public void setTemperature(BigDecimal temperature) {
        this.temperature = temperature;
    }

    public Integer getPulseRate() {
        return this.pulseRate;
    }

    public InpatientVitals pulseRate(Integer pulseRate) {
        this.setPulseRate(pulseRate);
        return this;
    }

    public void setPulseRate(Integer pulseRate) {
        this.pulseRate = pulseRate;
    }

    public Integer getSystolicBp() {
        return this.systolicBp;
    }

    public InpatientVitals systolicBp(Integer systolicBp) {
        this.setSystolicBp(systolicBp);
        return this;
    }

    public void setSystolicBp(Integer systolicBp) {
        this.systolicBp = systolicBp;
    }

    public Integer getDiastolicBp() {
        return this.diastolicBp;
    }

    public InpatientVitals diastolicBp(Integer diastolicBp) {
        this.setDiastolicBp(diastolicBp);
        return this;
    }

    public void setDiastolicBp(Integer diastolicBp) {
        this.diastolicBp = diastolicBp;
    }

    public Integer getOxygenSaturation() {
        return this.oxygenSaturation;
    }

    public InpatientVitals oxygenSaturation(Integer oxygenSaturation) {
        this.setOxygenSaturation(oxygenSaturation);
        return this;
    }

    public void setOxygenSaturation(Integer oxygenSaturation) {
        this.oxygenSaturation = oxygenSaturation;
    }

    public BigDecimal getWeight() {
        return this.weight;
    }

    public InpatientVitals weight(BigDecimal weight) {
        this.setWeight(weight);
        return this;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public BigDecimal getHeight() {
        return this.height;
    }

    public InpatientVitals height(BigDecimal height) {
        this.setHeight(height);
        return this;
    }

    public void setHeight(BigDecimal height) {
        this.height = height;
    }

    public BigDecimal getBmi() {
        return this.bmi;
    }

    public InpatientVitals bmi(BigDecimal bmi) {
        this.setBmi(bmi);
        return this;
    }

    public void setBmi(BigDecimal bmi) {
        this.bmi = bmi;
    }

    public String getNotes() {
        return this.notes;
    }

    public InpatientVitals notes(String notes) {
        this.setNotes(notes);
        return this;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Admission getAdmission() {
        return this.admission;
    }

    public void setAdmission(Admission admission) {
        this.admission = admission;
    }

    public InpatientVitals admission(Admission admission) {
        this.setAdmission(admission);
        return this;
    }

    public User getRecordedBy() {
        return this.recordedBy;
    }

    public void setRecordedBy(User user) {
        this.recordedBy = user;
    }

    public InpatientVitals recordedBy(User user) {
        this.setRecordedBy(user);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof InpatientVitals)) {
            return false;
        }
        return getId() != null && getId().equals(((InpatientVitals) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "InpatientVitals{" +
            "id=" + getId() +
            ", recordedAt='" + getRecordedAt() + "'" +
            ", temperature=" + getTemperature() +
            ", pulseRate=" + getPulseRate() +
            ", systolicBp=" + getSystolicBp() +
            ", diastolicBp=" + getDiastolicBp() +
            ", oxygenSaturation=" + getOxygenSaturation() +
            ", weight=" + getWeight() +
            ", height=" + getHeight() +
            ", bmi=" + getBmi() +
            ", notes='" + getNotes() + "'" +
            "}";
    }
}
