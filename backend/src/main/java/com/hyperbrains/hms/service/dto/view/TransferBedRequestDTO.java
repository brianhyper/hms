package com.hyperbrains.hms.service.dto.view;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * Moving a patient to another bed.
 *
 * <p>The reason is mandatory, and not as a formality. A move changes which ward is responsible for the
 * patient and, in practice, which doctors see them — it is the kind of decision that gets questioned
 * afterwards, and "why is this patient in an ordinary ward when they were in ICU this morning" has to be
 * answerable from the record rather than from whoever happens to remember.
 */
public class TransferBedRequestDTO implements Serializable {

    /** The bed the patient is moving to. Must be free and in a ward that is taking patients. */
    @NotNull
    private Long toBedId;

    /** Why the patient is being moved. */
    @NotBlank
    @Size(max = 10000)
    private String reason;

    public Long getToBedId() {
        return toBedId;
    }

    public void setToBedId(Long toBedId) {
        this.toBedId = toBedId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
