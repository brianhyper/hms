package com.hyperbrains.hms.service.dto.view;

import com.hyperbrains.hms.domain.enumeration.PrescriptionSource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * Writing a prescription against a visit.
 *
 * <p>The lines are all-or-nothing. A prescription that reserved stock for three drugs and then failed
 * on the fourth would leave the patient with a partly-filled order and the pharmacy holding a
 * reservation nobody dispenses — so stock is set aside for every line or for none.
 */
public class PlacePrescriptionRequestDTO implements Serializable {

    /** Where the prescription came from: one of our doctors, or an outside prescriber. */
    @NotNull
    private PrescriptionSource source;

    /** Required for an outside prescription, so the pharmacy knows who to query it with. */
    @Size(max = 255)
    private String prescribingSource;

    @NotEmpty
    @Size(max = 50, message = "A prescription with more than 50 drugs is almost certainly an error; split it")
    @Valid
    private List<PrescriptionLineRequestDTO> lines;

    /**
     * An outside prescription must say who wrote it.
     *
     * <p>Expressed as bean validation so the client gets an ordinary 400 field error rather than a
     * hand-rolled error shape.
     */
    @AssertTrue(message = "prescribingSource is required when source is EXTERNAL")
    public boolean isPrescribingSourceConsistent() {
        if (source == null || source != PrescriptionSource.EXTERNAL) {
            return true;
        }
        return prescribingSource != null && !prescribingSource.isBlank();
    }

    public PrescriptionSource getSource() {
        return source;
    }

    public void setSource(PrescriptionSource source) {
        this.source = source;
    }

    public String getPrescribingSource() {
        return prescribingSource;
    }

    public void setPrescribingSource(String prescribingSource) {
        this.prescribingSource = prescribingSource;
    }

    public List<PrescriptionLineRequestDTO> getLines() {
        return lines;
    }

    public void setLines(List<PrescriptionLineRequestDTO> lines) {
        this.lines = lines;
    }
}
