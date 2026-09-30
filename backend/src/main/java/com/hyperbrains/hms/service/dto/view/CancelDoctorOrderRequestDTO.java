package com.hyperbrains.hms.service.dto.view;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * Withdrawing an order before it is finished.
 *
 * <p>A reason is mandatory. An order that stops appearing on the ward's list, with nothing recorded to say
 * why, is the kind of gap that gets reconstructed from memory weeks later — and the medicine may already have
 * been reserved against it.
 */
public class CancelDoctorOrderRequestDTO implements Serializable {

    @NotBlank
    @Size(max = 10000)
    private String reason;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
