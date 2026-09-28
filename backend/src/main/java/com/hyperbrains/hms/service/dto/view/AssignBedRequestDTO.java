package com.hyperbrains.hms.service.dto.view;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Giving a patient a bed.
 *
 * <p>Only the bed: there is no reason field, because this is not a move. A patient going from one bed to
 * another is a ward transfer, which §4 of the specification models as its own recorded event with a
 * mandatory reason — moving somebody between wards changes who is responsible for them, and this action
 * has nothing to say about that.
 */
public class AssignBedRequestDTO implements Serializable {

    /** The bed the patient is being put into. */
    @NotNull
    private Long bedId;

    public Long getBedId() {
        return bedId;
    }

    public void setBedId(Long bedId) {
        this.bedId = bedId;
    }
}
