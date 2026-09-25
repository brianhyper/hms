package com.hyperbrains.hms.web.rest;

import com.hyperbrains.hms.service.dto.view.DispenseRecordDTO;
import com.hyperbrains.hms.service.dto.view.DispenseRequestDTO;
import com.hyperbrains.hms.service.dto.view.PrescriptionViewDTO;
import com.hyperbrains.hms.service.workflow.DispenseWorkflowService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Handing medicine over.
 *
 * <p>There is no endpoint here for marking a prescription ready to dispense, and that is the point:
 * reaching {@code READY_FOR_DISPENSE} happens only when the bill is settled, through the payment
 * workflow. A route that could set it directly would be a route that hands medicine over for free.
 */
@RestController
@RequestMapping("/api/pharmacy-dispense")
public class PharmacyDispenseResource {

    private final DispenseWorkflowService dispenseService;

    public PharmacyDispenseResource(DispenseWorkflowService dispenseService) {
        this.dispenseService = dispenseService;
    }

    /**
     * Hand over part or all of a prescription.
     *
     * <p>Refused unless the bill has been settled: the payment gate is enforced by the prescription's
     * own state, so pharmacy does not have to check the visit or the bill to know it is safe.
     */
    @PostMapping("/{prescriptionId}/dispense")
    public ResponseEntity<PrescriptionViewDTO> dispense(
        @PathVariable Long prescriptionId,
        @Valid @RequestBody DispenseRequestDTO request
    ) {
        return ResponseEntity.ok(dispenseService.dispense(prescriptionId, request));
    }

    /**
     * What has already been handed over for this prescription.
     *
     * <p>The only record of what physically left the shelf — the question a patient disputing their
     * medicine is really asking.
     */
    @GetMapping("/{prescriptionId}/history")
    public ResponseEntity<List<DispenseRecordDTO>> history(@PathVariable Long prescriptionId) {
        return ResponseEntity.ok(dispenseService.history(prescriptionId));
    }
}
