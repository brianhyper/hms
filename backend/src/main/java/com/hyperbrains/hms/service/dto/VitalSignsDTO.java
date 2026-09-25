package com.hyperbrains.hms.service.dto;

import jakarta.persistence.Lob;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * A DTO for the {@link com.hyperbrains.hms.domain.VitalSigns} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class VitalSignsDTO implements Serializable {

    private Long id;

    private BigDecimal temperature;

    private Integer pulseRate;

    private Integer systolicBp;

    private Integer diastolicBp;

    private Integer oxygenSaturation;

    private BigDecimal weight;

    private BigDecimal height;

    private BigDecimal bmi;

    @Size(max = 100)
    private String nutritionalStatus;

    private Boolean pregnancyScreening;
    private String triageNotes;
    private String otherMeasurements;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getNutritionalStatus() {
        return nutritionalStatus;
    }

    public void setNutritionalStatus(String nutritionalStatus) {
        this.nutritionalStatus = nutritionalStatus;
    }

    public Boolean getPregnancyScreening() {
        return pregnancyScreening;
    }

    public void setPregnancyScreening(Boolean pregnancyScreening) {
        this.pregnancyScreening = pregnancyScreening;
    }

    public String getTriageNotes() {
        return triageNotes;
    }

    public void setTriageNotes(String triageNotes) {
        this.triageNotes = triageNotes;
    }

    public String getOtherMeasurements() {
        return otherMeasurements;
    }

    public void setOtherMeasurements(String otherMeasurements) {
        this.otherMeasurements = otherMeasurements;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VitalSignsDTO)) {
            return false;
        }

        VitalSignsDTO vitalSignsDTO = (VitalSignsDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, vitalSignsDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "VitalSignsDTO{" +
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
