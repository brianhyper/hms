package com.hyperbrains.hms.service.dto.view;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * Withdrawing a prescription.
 *
 * <p>The reason is required, not optional. Withdrawing puts stock back on the shelf and takes money
 * off a bill, and an audit trail that cannot say why is not an audit trail. This mirrors the reason
 * demanded when a patient is selected out of queue order.
 */
public class CancelPrescriptionRequestDTO implements Serializable {

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
