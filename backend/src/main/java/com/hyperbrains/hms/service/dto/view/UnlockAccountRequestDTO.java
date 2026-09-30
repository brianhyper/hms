package com.hyperbrains.hms.service.dto.view;

import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * Releasing a sign-in lock.
 *
 * <p>{@code reason} is mandatory, and the service refuses without it rather than the DTO, so that the refusal
 * carries a business error key like every other reason in this system ("withdrawal requires a reason", "bed
 * maintenance requires a reason"). Nobody unlocks an account because they clicked the wrong row: the reason is
 * what makes the release reviewable afterwards.
 */
public class UnlockAccountRequestDTO implements Serializable {

    @Size(max = 10000)
    private String reason;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
