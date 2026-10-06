package com.hyperbrains.hms.service.dto.view;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * A request to record an override: an action that went ahead without its usual precondition.
 *
 * <p>It carries what was overridden and why, and deliberately not the actor or the role. Those are attributed by
 * {@code OverrideService} from the authenticated caller, never accepted from the request, for the same reason the
 * audit trail refuses a client-supplied actor: an override that the caller can attribute to somebody else is not a
 * control.
 */
public class OverrideRequestDTO implements Serializable {

    /** The affected record's simple name, e.g. {@code "Prescription"}. */
    @NotBlank
    @Size(max = 120)
    private String overriddenEntity;

    /** The affected row's id, as a string so any id type fits. */
    @NotBlank
    @Size(max = 100)
    private String overriddenEntityId;

    /** Why the precondition was set aside. The reason is the control that replaces it, so it is required. */
    @NotBlank
    @Size(max = 10000)
    private String reason;

    public String getOverriddenEntity() {
        return overriddenEntity;
    }

    public void setOverriddenEntity(String overriddenEntity) {
        this.overriddenEntity = overriddenEntity;
    }

    public String getOverriddenEntityId() {
        return overriddenEntityId;
    }

    public void setOverriddenEntityId(String overriddenEntityId) {
        this.overriddenEntityId = overriddenEntityId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
