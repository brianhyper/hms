package com.hyperbrains.hms.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.hyperbrains.hms.domain.InpatientVitals} entity.
 */
@Schema(
    description = "Inpatient observations. Separate from VitalSigns because that entity is unique\nper visit — a correct shape for one triage moment and the wrong one for a\npatient charted twice a day for a week."
)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class InpatientVitalsDTO implements Serializable {

    private Long id;

    @NotNull
    private Instant recordedAt;

    private BigDecimal temperature;

    private Integer pulseRate;

    private Integer systolicBp;

    private Integer diastolicBp;

    private Integer oxygenSaturation;

    private BigDecimal weight;

    private BigDecimal height;

    private BigDecimal bmi;

    @Size(max = 10000)
    private String notes;

    @NotNull
    private AdmissionDTO admission;

    @NotNull
    private UserDTO recordedBy;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(Instant recordedAt) {
        this.recordedAt = recordedAt;
    }

    public BigDecimal getTemperature() {
        return temperature;
    }

    public void setTemperature(BigDecimal temperature) {
        this.temperature = temperature;
    }

    public Integer getPulseRate() {
        return pulseRate;
    }

    public void setPulseRate(Integer pulseRate) {
        this.pulseRate = pulseRate;
    }

    public Integer getSystolicBp() {
        return systolicBp;
    }

    public void setSystolicBp(Integer systolicBp) {
        this.systolicBp = systolicBp;
    }

    public Integer getDiastolicBp() {
        return diastolicBp;
    }

    public void setDiastolicBp(Integer diastolicBp) {
        this.diastolicBp = diastolicBp;
    }

    public Integer getOxygenSaturation() {
        return oxygenSaturation;
    }

    public void setOxygenSaturation(Integer oxygenSaturation) {
        this.oxygenSaturation = oxygenSaturation;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public BigDecimal getHeight() {
        return height;
    }

    public void setHeight(BigDecimal height) {
        this.height = height;
    }

    public BigDecimal getBmi() {
        return bmi;
    }

    public void setBmi(BigDecimal bmi) {
        this.bmi = bmi;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public AdmissionDTO getAdmission() {
        return admission;
    }

    public void setAdmission(AdmissionDTO admission) {
        this.admission = admission;
    }

    public UserDTO getRecordedBy() {
        return recordedBy;
    }

    public void setRecordedBy(UserDTO recordedBy) {
        this.recordedBy = recordedBy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof InpatientVitalsDTO)) {
            return false;
        }

        InpatientVitalsDTO inpatientVitalsDTO = (InpatientVitalsDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, inpatientVitalsDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "InpatientVitalsDTO{" +
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
            ", admission=" + getAdmission() +
            ", recordedBy=" + getRecordedBy() +
            "}";
    }
}
