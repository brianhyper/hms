package com.hyperbrains.hms.service.workflow;

import com.hyperbrains.hms.service.dto.VisitDTO;
import com.hyperbrains.hms.service.dto.view.AppointmentCheckInRequestDTO;
import com.hyperbrains.hms.service.dto.view.VisitIntakeRequestDTO;

/**
 * Opening a visit — the single point at which a patient enters the clinical workflow.
 *
 * <p>There is no other way to create one. A visit is not a generic record that anyone may post; it
 * starts as an outpatient visit in a queue, with an owning reason and a priority, and everything
 * downstream (triage, consultation, billing, closing) hangs off that.
 */
public interface VisitIntakeService {

    /**
     * Turn a scheduled appointment into a visit, because the patient has physically arrived.
     *
     * <p>Only a {@code SCHEDULED} appointment can be checked in. That is the rule which keeps a
     * missed appointment final: a no-show is never quietly revived by a late arrival, and Reception
     * opens an ordinary walk-in visit instead, unrelated to the slot that was missed.
     *
     * @throws com.hyperbrains.hms.service.BusinessRuleViolationException if the appointment is not
     *         currently checkable.
     */
    VisitDTO checkIn(Long appointmentId, AppointmentCheckInRequestDTO request);

    /**
     * Open a visit for a patient with no usable appointment: a walk-in, an emergency arrival, or
     * someone collecting medication only.
     *
     * @throws com.hyperbrains.hms.service.BusinessRuleViolationException if the patient does not
     *         exist, has been merged into another record, or the requested type cannot be opened
     *         this way.
     */
    VisitDTO createVisit(VisitIntakeRequestDTO request);
}
