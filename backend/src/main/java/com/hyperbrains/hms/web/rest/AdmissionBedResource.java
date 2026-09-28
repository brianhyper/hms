package com.hyperbrains.hms.web.rest;

import com.hyperbrains.hms.service.dto.view.AdmissionBedResultDTO;
import com.hyperbrains.hms.service.dto.view.AssignBedRequestDTO;
import com.hyperbrains.hms.service.workflow.AdmissionWorkflowService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Putting a patient into a bed.
 *
 * <p>Mapped under {@code /api/admissions} because that is where a stay lives, and as a second controller on
 * that base path rather than an extra method on the generated CRUD resource — regenerating the entity
 * cannot overwrite this, which is the same reason Phase 1 kept its workflow endpoints out of the generated
 * resources.
 *
 * <p>The generated {@code PUT /api/admissions/{id}} is left in place for editing the record itself and is
 * restricted to Super Admin, since as raw CRUD it can set the status and the bed by hand and would
 * therefore make this action and its guards optional.
 */
@RestController
@RequestMapping("/api/admissions")
public class AdmissionBedResource {

    private static final Logger LOG = LoggerFactory.getLogger(AdmissionBedResource.class);

    private final AdmissionWorkflowService admissionWorkflowService;

    public AdmissionBedResource(AdmissionWorkflowService admissionWorkflowService) {
        this.admissionWorkflowService = admissionWorkflowService;
    }

    /**
     * {@code PUT /admissions/:admissionId/bed} : this patient is going into this bed.
     *
     * <p>{@code PUT} rather than {@code POST} because the target is being named, not added to a collection,
     * and answers {@code 200} with both ends of both changes rather than {@code 201}: the bed is not created
     * here, it is taken.
     */
    @PutMapping("/{admissionId}/bed")
    public ResponseEntity<AdmissionBedResultDTO> assignBed(
        @PathVariable("admissionId") Long admissionId,
        @Valid @RequestBody AssignBedRequestDTO request
    ) {
        LOG.debug("REST request to put admission {} into bed {}", admissionId, request.getBedId());
        return ResponseEntity.ok(admissionWorkflowService.assignBed(admissionId, request));
    }
}
