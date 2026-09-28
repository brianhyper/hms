package com.hyperbrains.hms.service.dto.view;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * Taking a bed out of service.
 *
 * <p>The reason is mandatory for the same class of reason as the admission reason: a bed that
 * disappears from the available list is invisible work, and the next person to look at the ward needs
 * to know whether it is a broken frame, a deep clean or a bed that has been wheeled somewhere else.
 */
public class MarkBedMaintenanceRequestDTO implements Serializable {

    /** Why the bed is out of service. */
    @NotBlank
    @Size(max = 500)
    private String reason;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
