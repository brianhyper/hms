package com.hyperbrains.hms.web.rest;

import com.hyperbrains.hms.service.dto.view.TransferBedRequestDTO;
import com.hyperbrains.hms.service.dto.view.WardTransferResultDTO;
import com.hyperbrains.hms.service.workflow.AdmissionWorkflowService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Moving a patient to another bed, with a reason.
 *
 * <p>{@code POST} rather than {@code PUT}, unlike placing a patient: this creates a record of something
 * that happened. A transfer is an event in the patient's stay — the row it leaves behind is the answer to
 * "where has this patient been" — whereas placing a patient into their first bed changes a state.
 *
 * <p>Named {@code WardTransferResource} rather than {@code AdmissionTransferResource}, which is the
 * generated CRUD resource for the same entity on {@code /api/admission-transfers}; regenerating that entity
 * cannot overwrite this action.
 */
@RestController
@RequestMapping("/api/admissions")
public class WardTransferResource {

    private static final Logger LOG = LoggerFactory.getLogger(WardTransferResource.class);

    private final AdmissionWorkflowService admissionWorkflowService;

    public WardTransferResource(AdmissionWorkflowService admissionWorkflowService) {
        this.admissionWorkflowService = admissionWorkflowService;
    }

    /**
     * {@code POST /admissions/:admissionId/transfers} : move this patient to another bed, and say why.
     *
     * <p>Answers {@code 200} with both ends of the move rather than {@code 201}: the resource the caller
     * asked for — the patient's location — has been replaced, and the transfer record is the receipt.
     */
    @PostMapping("/{admissionId}/transfers")
    public ResponseEntity<WardTransferResultDTO> transfer(
        @PathVariable("admissionId") Long admissionId,
        @Valid @RequestBody TransferBedRequestDTO request
    ) {
        LOG.debug("REST request to move admission {} to bed {}", admissionId, request.getToBedId());
        return ResponseEntity.ok(admissionWorkflowService.transfer(admissionId, request));
    }
}
