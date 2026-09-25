package com.hyperbrains.hms.service.dto.view;

import java.io.Serializable;

/**
 * A reading that was saved but looks concerning.
 *
 * <p>Returned alongside the saved vitals in a 2xx response. Carrying the value and the normal band
 * means the client can show "Pulse rate 130 is outside the normal range of 60-100 bpm" rather than a
 * bare "unusual reading" the nurse has to interpret.
 */
public class VitalsWarningDTO implements Serializable {

    /** Which reading, as a field name the client can map to an input. */
    private String field;

    private Double value;

    private Double warnMin;

    private Double warnMax;

    private String message;

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public Double getWarnMin() {
        return warnMin;
    }

    public void setWarnMin(Double warnMin) {
        this.warnMin = warnMin;
    }

    public Double getWarnMax() {
        return warnMax;
    }

    public void setWarnMax(Double warnMax) {
        this.warnMax = warnMax;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
