package com.hyperbrains.hms.service.workflow;

import com.hyperbrains.hms.service.dto.VisitDTO;
import com.hyperbrains.hms.service.dto.view.StartVitalsRequestDTO;
import com.hyperbrains.hms.service.dto.view.VitalsSubmissionRequestDTO;
import com.hyperbrains.hms.service.dto.view.VitalsSubmissionResultDTO;

/**
 * Triage: the nurse records vitals, and the visit moves itself into the doctor's queue.
 */
public interface TriageService {

    /**
     * Take a patient from the vitals queue.
     *
     * <p>Not strictly required before submitting — a nurse who submits directly claims the patient
     * implicitly — but it is what sets {@code IN_VITALS} and stamps {@code startedVitalsAt}, so the
     * queue can show who is being worked on and two nurses cannot both open the same patient.
     *
     * @throws com.hyperbrains.hms.service.BusinessRuleViolationException if the visit is not waiting
     *         for vitals, or if it is selected out of order without a reason.
     */
    VisitDTO startVitals(Long visitId, StartVitalsRequestDTO request);

    /**
     * Record vitals and move the visit to the doctor's queue.
     *
     * <p>Two outcomes, deliberately different in kind:
     * <ul>
     *   <li>Values outside the normal band are saved and returned as warnings.</li>
     *   <li>Values outside the physiologically possible band are refused with
     *       {@link com.hyperbrains.hms.service.VitalsRejectedException}.</li>
     * </ul>
     */
    VitalsSubmissionResultDTO submitVitals(Long visitId, VitalsSubmissionRequestDTO request);
}
