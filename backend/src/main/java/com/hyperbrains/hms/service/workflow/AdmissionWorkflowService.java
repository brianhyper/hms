package com.hyperbrains.hms.service.workflow;

import com.hyperbrains.hms.service.dto.view.AdmissionBedResultDTO;
import com.hyperbrains.hms.service.dto.view.AdmitPatientRequestDTO;
import com.hyperbrains.hms.service.dto.view.AssignBedRequestDTO;
import com.hyperbrains.hms.service.dto.view.TransferBedRequestDTO;
import com.hyperbrains.hms.service.dto.view.VisitAdmissionResultDTO;
import com.hyperbrains.hms.service.dto.view.WardTransferResultDTO;

/**
 * Turning an outpatient encounter into an admission, and putting the patient in a bed.
 *
 * <p>Named {@code ...WorkflowService} rather than {@code AdmissionService} to match the rest of the
 * clinical actions, and to leave the plain name free for the generated CRUD service, which is a different
 * thing entirely and is restricted to Super Admin.
 *
 * <p><strong>Scope of Phase 1.</strong> The specification is explicit that only the conversion and the
 * type change are in scope: the visit's type becomes {@code ADMISSION} in place, everything already
 * recorded stays attached, and the outpatient path toward payment and closure is bypassed — an admitted
 * patient's billing accumulates over the length of the stay under rules that will be defined with the
 * inpatient domain. There is deliberately no bed, ward, attending-consultant or discharge concept here,
 * and charges already raised are left exactly as they are rather than voided, because "the stay
 * accumulates" and "the outpatient charges disappear" cannot both be true.
 *
 * <p><strong>Scope of Phase 2, slice 3.</strong> The conversion now also opens an {@code Admission} record,
 * in {@code PENDING_BED}, and a bed can be given to it. Discharge, ward transfer, charting, orders and
 * stay billing are later slices, each with their own endpoints.
 */
public interface AdmissionWorkflowService {

    /**
     * Convert this visit to an admission and open the stay.
     *
     * <p>The two halves are not the same moment, and the specification is emphatic about it:
     * {@code VisitStatus.ADMITTED} is set when the doctor decides, while {@code AdmissionStatus.ADMITTED}
     * waits until a bed is found.
     *
     * @throws com.hyperbrains.hms.service.BusinessRuleViolationException if there is no such visit, the
     *         visit is already an admission, the encounter is already over, nobody has assessed the
     *         patient for admission, or a stay has already been opened for this visit
     */
    VisitAdmissionResultDTO admit(Long visitId, AdmitPatientRequestDTO request);

    /**
     * Put this patient into a bed: the admission reaches {@code ADMITTED} and the bed {@code OCCUPIED}.
     *
     * <p>This is the only way a bed becomes occupied. The generated bed CRUD cannot do it — it can set any
     * status at all — which is exactly why it is restricted to Super Admin.
     *
     * <p>Not a move. An admission that already holds a bed is refused here and has to go through a ward
     * transfer, which §4 of the specification models as its own recorded event with a mandatory reason.
     *
     * @throws com.hyperbrains.hms.service.BusinessRuleViolationException if there is no such admission or
     *         bed, the stay is not waiting for a bed, the bed is not free, the bed's ward is closed, or this
     *         patient already has another stay open
     */
    AdmissionBedResultDTO assignBed(Long admissionId, AssignBedRequestDTO request);

    /**
     * Move a patient to another bed, and record why.
     *
     * <p>"Another bed" includes another ward — an ICU patient stepped down to an ordinary ward is the
     * ordinary case, and it is the reason this exists rather than the patient simply being placed again.
     * The bed being left goes to {@code CLEANING}, never straight back to {@code AVAILABLE}: it has just
     * been occupied and somebody has to say it is ready.
     *
     * <p>The admission's bed is the only record of where the patient currently is, so the ward a doctor is
     * judged against ("am I covering this ward") follows the move with no further work. That is deliberate:
     * anything that cached the ward would be wrong from the first transfer onwards.
     *
     * @throws com.hyperbrains.hms.service.BusinessRuleViolationException if there is no such admission or
     *         bed, the reason is missing, the stay has no bed to move from, the stay is over, the bed is not
     *         free, it is the bed the patient is already in, or its ward is closed
     */
    WardTransferResultDTO transfer(Long admissionId, TransferBedRequestDTO request);
}
