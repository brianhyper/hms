package com.hyperbrains.hms.service.dto.view;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * Connecting a temporary patient record to the real one.
 *
 * <p>Both ids are named explicitly rather than one being implied by the route: a merge destroys an
 * identity, and an endpoint where the direction is implied by which id happens to be in the path is an
 * endpoint where the direction can be got wrong silently.
 */
public class MergePatientRequestDTO implements Serializable {

    /** The temporary record. It stops being a patient and becomes a pointer to the target. */
    @NotNull
    private Long sourcePatientId;

    /** The confirmed record. It keeps its hospital number and absorbs the source's clinical history. */
    @NotNull
    private Long targetPatientId;

    /** Why these two records are the same person. Required: a merge has no other evidence attached to it. */
    @NotBlank
    @Size(max = 10000)
    private String reason;

    public Long getSourcePatientId() {
        return sourcePatientId;
    }

    public void setSourcePatientId(Long sourcePatientId) {
        this.sourcePatientId = sourcePatientId;
    }

    public Long getTargetPatientId() {
        return targetPatientId;
    }

    public void setTargetPatientId(Long targetPatientId) {
        this.targetPatientId = targetPatientId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
