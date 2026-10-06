package com.hyperbrains.hms.service.workflow;

import com.hyperbrains.hms.service.dto.view.DischargeSignOffRequestDTO;
import com.hyperbrains.hms.service.dto.view.DischargeViewDTO;

/**
 * Ending a stay, in two signatures.
 *
 * <p>Two actions and not one, which is Phase 2 §7's own argument: a single endpoint taking two names would let one
 * caller claim both sign-offs, and that is the thing the second signature exists to prevent. Each action records its
 * own actor, its own instant and its own audit entry, and the stay reaches {@code DISCHARGED} only when both are in.
 *
 * <p>There is deliberately no method for death in hospital or for discharge against medical advice. §7 says neither
 * is a discharge and neither should need two signatures; whether they get their own status and who signs them off is
 * §11 question 4 and is unanswered, so nothing here invents an answer.
 */
public interface InpatientDischargeService {
    /**
     * The doctor's half of a discharge. The stay is only ended when the nurse's half is also in.
     *
     * @param admissionId the stay being ended.
     * @param request the note, and the acknowledgement required when orders are still running.
     * @return the discharge as it stands after this signature.
     */
    DischargeViewDTO doctorSignsOff(Long admissionId, DischargeSignOffRequestDTO request);

    /**
     * The nurse's half of a discharge, on the same terms.
     *
     * @param admissionId the stay being ended.
     * @param request the note, and the acknowledgement required when orders are still running.
     * @return the discharge as it stands after this signature.
     */
    DischargeViewDTO nurseSignsOff(Long admissionId, DischargeSignOffRequestDTO request);
}
