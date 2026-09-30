package com.hyperbrains.hms.web.rest;

import com.hyperbrains.hms.service.dto.view.CancelDoctorOrderRequestDTO;
import com.hyperbrains.hms.service.dto.view.DoctorOrderViewDTO;
import com.hyperbrains.hms.service.dto.view.ExecuteOrderRequestDTO;
import com.hyperbrains.hms.service.dto.view.PlaceDoctorOrderRequestDTO;
import com.hyperbrains.hms.service.workflow.DoctorOrderWorkflowService;
import jakarta.validation.Valid;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Doctor's orders on the ward: what was written, what has been done about it, and stopping it.
 *
 * <p>Named {@code InpatientOrderResource} and on its own path: the generated CRUD for {@code DoctorOrder}
 * already owns {@code /api/doctor-orders} and its {@code GET /{id}} route swallows any single segment, so a
 * sub-route there would be an ambiguous mapping — which fails the whole application context, not one request.
 *
 * <p>The generated CRUD is restricted to Super Admin, because as raw CRUD it can set an order's status
 * directly and would therefore make every action here optional.
 */
@RestController
@RequestMapping("/api/inpatient-orders")
public class InpatientOrderResource {

    private static final Logger LOG = LoggerFactory.getLogger(InpatientOrderResource.class);

    private final DoctorOrderWorkflowService orderService;

    public InpatientOrderResource(DoctorOrderWorkflowService orderService) {
        this.orderService = orderService;
    }

    /** {@code POST /inpatient-orders/:admissionId} : write an order on this stay. */
    @PostMapping("/{admissionId}")
    public ResponseEntity<DoctorOrderViewDTO> place(
        @PathVariable("admissionId") Long admissionId,
        @Valid @RequestBody PlaceDoctorOrderRequestDTO request
    ) {
        LOG.debug("REST request to place a {} order on admission {}", request.getType(), admissionId);
        return ResponseEntity.ok(orderService.place(admissionId, request));
    }

    /**
     * {@code POST /inpatient-orders/:orderId/executions} : a nurse records carrying it out.
     *
     * <p>{@code POST} because an execution is an event that happened, and there may be many of them.
     */
    @PostMapping("/{orderId}/executions")
    public ResponseEntity<DoctorOrderViewDTO> execute(
        @PathVariable("orderId") Long orderId,
        @RequestBody(required = false) ExecuteOrderRequestDTO request
    ) {
        LOG.debug("REST request to record execution of order {}", orderId);
        return ResponseEntity.ok(orderService.execute(orderId, request));
    }

    /** {@code PUT /inpatient-orders/:orderId/complete} : the prescriber stops a course that is finished. */
    @PutMapping("/{orderId}/complete")
    public ResponseEntity<DoctorOrderViewDTO> complete(@PathVariable("orderId") Long orderId) {
        LOG.debug("REST request to complete order {}", orderId);
        return ResponseEntity.ok(orderService.complete(orderId));
    }

    /** {@code PUT /inpatient-orders/:orderId/cancel} : withdraw an order, with a reason. */
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<DoctorOrderViewDTO> cancel(
        @PathVariable("orderId") Long orderId,
        @Valid @RequestBody CancelDoctorOrderRequestDTO request
    ) {
        LOG.debug("REST request to cancel order {}", orderId);
        return ResponseEntity.ok(orderService.cancel(orderId, request));
    }

    /** {@code GET /inpatient-orders/admission/:admissionId} : the order sheet, newest first. */
    @GetMapping("/admission/{admissionId}")
    public ResponseEntity<List<DoctorOrderViewDTO>> orderSheet(@PathVariable("admissionId") Long admissionId) {
        LOG.debug("REST request for the order sheet of admission {}", admissionId);
        return ResponseEntity.ok(orderService.orderSheet(admissionId));
    }
}
