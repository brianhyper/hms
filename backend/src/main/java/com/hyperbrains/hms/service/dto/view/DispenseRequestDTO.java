package com.hyperbrains.hms.service.dto.view;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * Handing medicine over at the counter.
 *
 * <p>Partial by design: a counter hands over what it has and the rest later, so this lists only the
 * lines and quantities being given out now. Everything not mentioned stays outstanding, and the
 * prescription's own status follows from what is left.
 */
public class DispenseRequestDTO implements Serializable {

    @Size(max = 10000)
    private String note;

    /**
     * Why medicine is being released before the bill is settled. Required only for a break-glass release, absent on
     * an ordinary hand-over: the reason is the control that replaces the payment gate, so it is never optional there.
     */
    @Size(max = 10000)
    private String overrideReason;

    @NotEmpty
    @Size(max = 50)
    @Valid
    private List<DispenseLineRequestDTO> lines;

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getOverrideReason() {
        return overrideReason;
    }

    public void setOverrideReason(String overrideReason) {
        this.overrideReason = overrideReason;
    }

    public List<DispenseLineRequestDTO> getLines() {
        return lines;
    }

    public void setLines(List<DispenseLineRequestDTO> lines) {
        this.lines = lines;
    }
}
