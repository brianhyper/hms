package com.hyperbrains.hms.service.dto.view;

import com.hyperbrains.hms.domain.enumeration.VisitPriority;
import com.hyperbrains.hms.domain.enumeration.VisitType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * Opening a visit for a patient who has no usable appointment — a walk-in, an emergency arrival,
 * or someone coming only to collect medication.
 *
 * <p>There is no registration-status precondition here, and that is deliberate: an unidentified
 * emergency patient has an incomplete registration, and refusing to open a visit for them would be
 * refusing treatment.
 */
public class VisitIntakeRequestDTO implements Serializable {

    @NotNull
    private Long patientId;

    /**
     * Which kind of visit to open.
     *
     * <p>Not {@code @NotNull} here on purpose, because one of the endpoints decides the type
     * itself: the pharmacy-only path can only ever create a pharmacy-only visit, so requiring the
     * client to send a value that is then ignored would be a validation trap. The service rejects
     * a missing type on the endpoint that genuinely needs one.
     */
    private VisitType type;

    /** Defaults to {@link VisitPriority#NORMAL} when omitted. */
    private VisitPriority priority;

    @NotNull
    @Size(max = 10000)
    private String reasonForVisit;

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public VisitType getType() {
        return type;
    }

    public void setType(VisitType type) {
        this.type = type;
    }

    public VisitPriority getPriority() {
        return priority;
    }

    public void setPriority(VisitPriority priority) {
        this.priority = priority;
    }

    public String getReasonForVisit() {
        return reasonForVisit;
    }

    public void setReasonForVisit(String reasonForVisit) {
        this.reasonForVisit = reasonForVisit;
    }
}
