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

    public List<DispenseLineRequestDTO> getLines() {
        return lines;
    }

    public void setLines(List<DispenseLineRequestDTO> lines) {
        this.lines = lines;
    }
}
