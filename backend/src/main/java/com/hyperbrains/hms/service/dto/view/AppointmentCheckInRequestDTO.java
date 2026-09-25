package com.hyperbrains.hms.service.dto.view;

import com.hyperbrains.hms.domain.enumeration.VisitPriority;
import java.io.Serializable;

/**
 * Checking an appointment in is an explicit act performed when the patient physically arrives.
 *
 * <p>Deliberately tiny: the appointment already knows the patient, the department and the doctor,
 * so all that can differ at the desk is how urgent this looks and why the patient came.
 */
public class AppointmentCheckInRequestDTO implements Serializable {

    /** Defaults to {@link VisitPriority#NORMAL} when omitted. */
    private VisitPriority priority;

    /** Replaces the appointment's own reason when the patient reports something different. */
    private String reasonForVisit;

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
