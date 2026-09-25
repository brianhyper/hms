package com.hyperbrains.hms.service.workflow;

import com.hyperbrains.hms.service.dto.view.PlacePrescriptionRequestDTO;
import com.hyperbrains.hms.service.dto.view.PrescriptionBillableDTO;
import com.hyperbrains.hms.service.dto.view.PrescriptionViewDTO;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Prescribing, from the doctor writing it to the pharmacy being allowed to hand it over.
 *
 * <p>Named {@code ...Workflow...} rather than {@code PrescriptionService} because the generated CRUD
 * service already owns that bean name.
 *
 * <p>The interesting guarantee lives in {@link #place}: writing a prescription is the moment the
 * medicine is set aside, not the moment it is handed over. That is what lets Finance bill for
 * medicine knowing it genuinely exists, without Pharmacy having to double-check anything.
 */
public interface PrescriptionWorkflowService {

    /**
     * Write a prescription against a visit, reserving stock and raising the charge.
     *
     * <p>All lines succeed or none do: a prescription that reserved half its drugs would leave the
     * pharmacy holding a reservation nobody dispenses.
     *
     * @throws com.hyperbrains.hms.service.BusinessRuleViolationException if the visit is closed, a
     *         drug is missing or withdrawn, or any line is short of availability
     */
    PrescriptionViewDTO place(Long visitId, PlacePrescriptionRequestDTO request);

    /** Everything prescribed during a visit, for the treating clinician. */
    List<PrescriptionViewDTO> forVisit(Long visitId);

    /**
     * Withdraw a prescription that will never be handed over.
     *
     * <p>Gives the reserved stock back to the shelf and takes the charge off the bill, so the
     * availability figure and the money both follow reality again.
     *
     * <p>Only possible before payment, and impossible once any of the medicine has been handed over.
     * A reason is mandatory: stock and money moving without a stated cause is exactly what an audit
     * cannot reconstruct.
     *
     * @throws com.hyperbrains.hms.service.BusinessRuleViolationException if the prescription is
     *         already paid, cancelled, or partly dispensed, or if no reason was given
     */
    PrescriptionViewDTO cancel(Long prescriptionId, String reason);

    /**
     * The pharmacy's dispensing queue: prescriptions whose bill has been paid.
     *
     * <p>A prescription reaches this queue only through {@link #markPaid}, which is the explicit
     * signal that lets Pharmacy hand medicine over without inspecting the Visit or the Bill.
     */
    Page<PrescriptionViewDTO> pharmacyQueue(Pageable pageable);

    /** Billing view of a visit's medicine: drug, quantity and money, with no posology. */
    List<PrescriptionBillableDTO> billableForVisit(Long visitId);

    /**
     * The bill this prescription belongs to has been settled, so pharmacy may hand it over.
     *
     * <p>Idempotent, because the payment step that calls it is itself retried: a prescription that is
     * already dispensable is left alone rather than treated as an error.
     */
    PrescriptionViewDTO markPaid(Long prescriptionId);
}
