package com.hyperbrains.hms.service.workflow;

import com.hyperbrains.hms.service.dto.view.AdmitPatientRequestDTO;
import com.hyperbrains.hms.service.dto.view.VisitAdmissionResultDTO;

/**
 * Turning an outpatient encounter into an admission.
 *
 * <p>Named {@code ...WorkflowService} rather than {@code AdmissionService} to match the rest of the
 * clinical actions, and to leave the plain name free for whatever the inpatient domain needs when it is
 * designed.
 *
 * <p><strong>Scope of Phase 1.</strong> The specification is explicit that only the conversion and the
 * type change are in scope: the visit's type becomes {@code ADMISSION} in place, everything already
 * recorded stays attached, and the outpatient path toward payment and closure is bypassed — an admitted
 * patient's billing accumulates over the length of the stay under rules that will be defined with the
 * inpatient domain. There is deliberately no bed, ward, attending-consultant or discharge concept here,
 * and charges already raised are left exactly as they are rather than voided, because "the stay
 * accumulates" and "the outpatient charges disappear" cannot both be true.
 */
public interface AdmissionWorkflowService {

    /**
     * Convert this visit to an admission.
     *
     * @throws com.hyperbrains.hms.service.BusinessRuleViolationException if there is no such visit, the
     *         visit is already an admission, the encounter is already over, or nobody has assessed the
     *         patient for admission
     */
    VisitAdmissionResultDTO admit(Long visitId, AdmitPatientRequestDTO request);
}
