package com.hyperbrains.hms.service.dto.view;

import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Vital signs as recorded by a nurse.
 *
 * <p>Every numeric field is optional: a partial set is normal, and demanding all of them would
 * push staff to invent values. Whatever is absent is stored as absent, which is itself information.
 *
 * <p>Re-submitting for a visit that already has vitals is a <strong>correction</strong> and requires
 * {@link #correctionReason}. The payload is treated as the complete reading set, so a field left
 * out is cleared — that is what makes it possible to remove a value that was entered by mistake.
 * There is deliberately no {@code bmi} field: it is derived from weight and height server-side, and a
 * derived figure that can disagree with its own inputs is worse than none.
 */
public class VitalsSubmissionRequestDTO implements Serializable {

    private BigDecimal temperature;

    private Integer pulseRate;

    private Integer systolicBp;

    private Integer diastolicBp;

    private Integer oxygenSaturation;

    private BigDecimal weight;

    private BigDecimal height;

    @Size(max = 100)
    private String nutritionalStatus;

    private Boolean pregnancyScreening;

    @Size(max = 10000)
    private String triageNotes;

    @Size(max = 10000)
    private String otherMeasurements;

    /** Required when replacing vitals that were already recorded for this visit. */
    @Size(max = 10000)
    private String correctionReason;

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

    public String getCorrectionReason() {
        return correctionReason;
    }

    public void setCorrectionReason(String correctionReason) {
        this.correctionReason = correctionReason;
    }
}
