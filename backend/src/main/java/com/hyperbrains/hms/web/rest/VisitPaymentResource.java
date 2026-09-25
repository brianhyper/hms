package com.hyperbrains.hms.web.rest;

import com.hyperbrains.hms.service.dto.view.BillViewDTO;
import com.hyperbrains.hms.service.dto.view.RecordPaymentRequestDTO;
import com.hyperbrains.hms.service.workflow.PaymentWorkflowService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Collecting the money for a visit.
 *
 * <p>Separate from the generated {@code /api/payments} CRUD because recording a payment row is not the
 * same act as settling a bill: this endpoint also decides whether the bill is discharged, and, if it
 * is, releases the visit's medicine to the pharmacy. A plain POST does none of that, which is why the
 * entity endpoint's writes are closed to every role but the super-admin.
 */
@RestController
@RequestMapping("/api/visit-payments")
public class VisitPaymentResource {

    private final PaymentWorkflowService paymentService;

    public VisitPaymentResource(PaymentWorkflowService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * What a visit owes, and what has been collected so far.
     *
     * <p>Readable by the desk as well as Finance: a receptionist telling a patient what to pay is the
     * common case, and it needs no billing authority.
     */
    @GetMapping("/bill/{visitId}")
    public ResponseEntity<BillViewDTO> bill(@PathVariable Long visitId) {
        return ResponseEntity.ok(paymentService.billForVisit(visitId));
    }

    /**
     * Record money received.
     *
     * <p>Refused before the visit reaches the payment stage, because until then the total is still
     * moving. A payment that covers the balance settles the bill, closes the visit and releases any
     * prescriptions to the pharmacy; one that does not is still recorded, so money being held is never
     * invisible.
     */
    @PostMapping("/{visitId}/pay")
    public ResponseEntity<BillViewDTO> pay(
        @PathVariable Long visitId,
        @Valid @RequestBody RecordPaymentRequestDTO request
    ) {
        return ResponseEntity.ok(paymentService.recordPayment(visitId, request));
    }
}
