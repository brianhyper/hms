package com.hyperbrains.hms.service.workflow;

import com.hyperbrains.hms.service.dto.view.CancelDoctorOrderRequestDTO;
import com.hyperbrains.hms.service.dto.view.DoctorOrderViewDTO;
import com.hyperbrains.hms.service.dto.view.ExecuteOrderRequestDTO;
import com.hyperbrains.hms.service.dto.view.PlaceDoctorOrderRequestDTO;
import java.util.List;

/**
 * Doctor's orders on the ward, and a nurse recording that one was carried out.
 *
 * <p>The rule this service exists to keep is that a drug order is not a second way to prescribe. Placing a
 * DRUG order places the {@code Prescription} that supplies it and links the two, so the ward has one
 * instruction to act on and the pharmacy has one supply record with its stock reservation, its queue position
 * and its charge. There is no way to place a drug order without the medicine behind it.
 *
 * <p>Nothing here schedules anything. {@code frequency} is what the doctor wrote; there is no due time, no
 * overdue flag and no scheduler in v1.0, and the order sheet is the list the ward works from.
 */
public interface DoctorOrderWorkflowService {

    /**
     * Place an order on a stay.
     *
     * <p>A DRUG order also writes the prescription that supplies it, in the same transaction, so an order whose
     * medicine could not be reserved does not exist either.
     *
     * @throws com.hyperbrains.hms.service.BusinessRuleViolationException if there is no such stay, the stay is
     *         over, a drug order names no drug or quantity, or the medicine is short of stock
     */
    DoctorOrderViewDTO place(Long admissionId, PlaceDoctorOrderRequestDTO request);

    /**
     * Record that an order was carried out.
     *
     * <p>A one-off order is finished by doing it. A recurring one is not: it stays running until the prescriber
     * stops it, because "the course is over" is a clinical judgement, not a side effect of the last dose being
     * charted.
     */
    DoctorOrderViewDTO execute(Long orderId, ExecuteOrderRequestDTO request);

    /** Stop a course the prescriber considers finished. */
    DoctorOrderViewDTO complete(Long orderId);

    /** Withdraw an order that should never have been written, or is no longer wanted. */
    DoctorOrderViewDTO cancel(Long orderId, CancelDoctorOrderRequestDTO request);

    /**
     * The orders written for this stay, newest first, each with what has been done about it.
     *
     * <p>Filtered by the inpatient access rule, because it is reached by an admission id.
     */
    List<DoctorOrderViewDTO> orderSheet(Long admissionId);
}
