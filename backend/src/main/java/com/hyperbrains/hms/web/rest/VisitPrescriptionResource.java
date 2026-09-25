package com.hyperbrains.hms.web.rest;

import com.hyperbrains.hms.service.dto.view.CancelPrescriptionRequestDTO;
import com.hyperbrains.hms.service.dto.view.PlacePrescriptionRequestDTO;
import com.hyperbrains.hms.service.dto.view.PrescriptionBillableDTO;
import com.hyperbrains.hms.service.dto.view.PrescriptionViewDTO;
import com.hyperbrains.hms.service.workflow.PrescriptionWorkflowService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * The prescribing workflow.
 *
 * <p>Separate from the generated {@code /api/prescriptions} CRUD because writing a row is not the
 * same act as prescribing: prescribing reserves stock and raises the charge, and a plain POST does
 * neither. That is why the entity endpoint's writes are closed to every role but the super-admin.
 *
 * <p>Note what is deliberately absent: there is no endpoint for marking a prescription paid. Payment
 * is the only thing that may release medicine to the pharmacy, and it is driven by the bill (see the
 * payment workflow). Exposing it here would let anyone with prescribing rights hand medicine over
 * without any money changing hands.
 */
@RestController
@RequestMapping("/api/visit-prescriptions")
public class VisitPrescriptionResource {

    private final PrescriptionWorkflowService prescriptionService;

    public VisitPrescriptionResource(PrescriptionWorkflowService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    /**
     * Write a prescription against a visit.
     *
     * <p>A doctor uses this during or after a consultation; pharmacy uses it for a walk-in carrying a
     * prescription from an outside prescriber. Either way it reserves the stock and charges for it.
     */
    @PostMapping("/{visitId}/place")
    public ResponseEntity<PrescriptionViewDTO> place(
        @PathVariable Long visitId,
        @Valid @RequestBody PlacePrescriptionRequestDTO request
    ) {
        PrescriptionViewDTO prescription = prescriptionService.place(visitId, request);
        return ResponseEntity.created(URI.create("/api/prescriptions/" + prescription.getPrescriptionId())).body(prescription);
    }

    /**
     * Withdraw a prescription before it is paid for.
     *
     * <p>The stock goes back on the shelf and the charge comes off the bill, so both follow reality
     * again. Only possible while the money is still owed: once it has been taken, the medicine is owed
     * too, and undoing that is a refund rather than a cancellation.
     */
    @PostMapping("/{prescriptionId}/cancel")
    public ResponseEntity<PrescriptionViewDTO> cancel(
        @PathVariable Long prescriptionId,
        @Valid @RequestBody CancelPrescriptionRequestDTO request
    ) {
        return ResponseEntity.ok(prescriptionService.cancel(prescriptionId, request.getReason()));
    }

    /** Everything prescribed during one visit, for the treating clinician. */
    @GetMapping("/visit/{visitId}")
    public ResponseEntity<List<PrescriptionViewDTO>> forVisit(@PathVariable Long visitId) {
        return ResponseEntity.ok(prescriptionService.forVisit(visitId));
    }

    /**
     * The pharmacy's dispensing queue.
     *
     * <p>Only prescriptions whose bill is already settled appear here, so Pharmacy can hand medicine
     * over without checking the Visit or the Bill.
     */
    @GetMapping("/pharmacy-queue")
    public ResponseEntity<Page<PrescriptionViewDTO>> pharmacyQueue(Pageable pageable) {
        return ResponseEntity.ok(prescriptionService.pharmacyQueue(pageable));
    }

    /**
     * What a visit's medicine costs, for the billing desk.
     *
     * <p>Drug, quantity and money only. The dosage and duration are the prescriber's clinical
     * instructions, and Finance has no reason to read them to reconcile an invoice.
     */
    @GetMapping("/billable/{visitId}")
    public ResponseEntity<List<PrescriptionBillableDTO>> billable(@PathVariable Long visitId) {
        return ResponseEntity.ok(prescriptionService.billableForVisit(visitId));
    }
}
