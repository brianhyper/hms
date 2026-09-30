package com.hyperbrains.hms.service.dto.view;

import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Vitals as charted on the ward.
 *
 * <p>The same field set and the same two-tier validation as outpatient triage — a pulse of 400 is refused
 * at the ward exactly as it is at the desk, because both go through {@code VitalsValidator}. What differs
 * is only how often it happens: an inpatient is charted many times over many days.
 *
 * <p>Every reading is optional, as it is in triage. A partial set is normal, and demanding all of them
 * would push staff to invent values. There is deliberately no {@code bmi}: it is derived from weight and
 * height on the server so that it cannot contradict the figures recorded beside it.
 */
public class ChartVitalsRequestDTO implements Serializable {

    private BigDecimal temperature;

    private Integer pulseRate;

    private Integer systolicBp;

    private Integer diastolicBp;

    private Integer oxygenSaturation;

    private BigDecimal weight;

    private BigDecimal height;

    @Size(max = 10000)
    private String notes;

    /**
     * Required when superseding an earlier observation, which is done through the corrections endpoint
     * rather than by charting over the top. A clinical record that can be quietly altered is not a record.
     */
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getCorrectionReason() {
        return correctionReason;
    }

    public void setCorrectionReason(String correctionReason) {
        this.correctionReason = correctionReason;
    }
}
