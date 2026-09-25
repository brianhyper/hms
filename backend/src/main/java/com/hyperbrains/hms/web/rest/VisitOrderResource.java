package com.hyperbrains.hms.web.rest;

import com.hyperbrains.hms.service.dto.DiagnosticOrderDTO;
import com.hyperbrains.hms.service.dto.view.EnterResultRequestDTO;
import com.hyperbrains.hms.service.dto.view.OrderSummaryDTO;
import com.hyperbrains.hms.service.dto.view.OrderWorklistItemDTO;
import com.hyperbrains.hms.service.dto.view.PlaceDiagnosticOrderRequestDTO;
import com.hyperbrains.hms.service.workflow.DiagnosticOrderWorkflowService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * The lab and radiology order workflow.
 *
 * <p>Separate from the generated {@code /api/diagnostic-orders} CRUD on purpose. Creating a row and
 * ordering a test are not the same operation: the workflow also prices the order, records it against
 * the visit and, when a result comes back, charges for it and re-evaluates whether the visit can
 * move on. A plain POST to the entity endpoint does none of those, so that endpoint is closed to
 * every role but the super-admin.
 *
 * <p>The two read endpoints that are not a single order exist because the audiences differ. A
 * worklist is scoped to the caller's discipline and carries identifying details only; the billable
 * view is a price list, with no clinical content at all.
 */
@RestController
@RequestMapping("/api/visit-orders")
public class VisitOrderResource {

    private final DiagnosticOrderWorkflowService diagnosticOrderService;

    public VisitOrderResource(DiagnosticOrderWorkflowService diagnosticOrderService) {
        this.diagnosticOrderService = diagnosticOrderService;
    }

    /** A doctor ordering a test against a visit. */
    @PostMapping("/{visitId}/place")
    public ResponseEntity<DiagnosticOrderDTO> place(
        @PathVariable Long visitId,
        @Valid @RequestBody PlaceDiagnosticOrderRequestDTO request
    ) {
        DiagnosticOrderDTO order = diagnosticOrderService.place(visitId, request);
        return ResponseEntity.created(URI.create("/api/diagnostic-orders/" + order.getId())).body(order);
    }

    /** A lab or radiology user recording what came back. Completes the order and charges for it. */
    @PostMapping("/{orderId}/result")
    public ResponseEntity<DiagnosticOrderDTO> enterResult(
        @PathVariable Long orderId,
        @Valid @RequestBody EnterResultRequestDTO request
    ) {
        return ResponseEntity.ok(diagnosticOrderService.enterResult(orderId, request));
    }

    /**
     * Abandon an order that will never be completed.
     *
     * <p>A cancellation is not a deletion: the record of what was asked for, and why it was dropped,
     * stays in the visit.
     */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<DiagnosticOrderDTO> cancel(@PathVariable Long orderId) {
        return ResponseEntity.ok(diagnosticOrderService.cancel(orderId));
    }

    /** The caller's own worklist, oldest request first. */
    @GetMapping("/worklist")
    public ResponseEntity<Page<OrderWorklistItemDTO>> worklist(Pageable pageable) {
        return ResponseEntity.ok(diagnosticOrderService.worklist(pageable));
    }

    /** Everything ordered for one visit, so the treating clinician can follow it up. */
    @GetMapping("/visit/{visitId}")
    public ResponseEntity<List<OrderWorklistItemDTO>> forVisit(@PathVariable Long visitId) {
        return ResponseEntity.ok(diagnosticOrderService.forVisit(visitId));
    }

    /**
     * What a visit's tests will cost, for the billing desk.
     *
     * <p>Returns names and prices only — no notes, no findings. Finance reconciling an invoice does
     * not need to read the clinical reasoning behind it.
     */
    @GetMapping("/billable/{visitId}")
    public ResponseEntity<List<OrderSummaryDTO>> billable(@PathVariable Long visitId) {
        return ResponseEntity.ok(diagnosticOrderService.billableForVisit(visitId));
    }
}
