package com.hyperbrains.hms.web.rest;

import com.hyperbrains.hms.service.dto.view.BedStatusViewDTO;
import com.hyperbrains.hms.service.dto.view.MarkBedMaintenanceRequestDTO;
import com.hyperbrains.hms.service.workflow.BedWorkflowService;
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
 * The two status actions staff perform on a bed: it is ready, or it is out of service.
 *
 * <p>Mapped under {@code /api/beds} because that is where a bed lives and because the specification
 * names {@code PUT /api/beds/{id}/available} explicitly. It is a second controller on the same base
 * path as the generated CRUD rather than an extra method on it, so that regenerating the entity cannot
 * overwrite these actions — the same reason Phase 1 kept its workflow endpoints out of the generated
 * resources.
 *
 * <p>The generated {@code PUT /api/beds/{id}} is left in place for editing the bed itself and is
 * restricted to Super Admin, since as raw CRUD it can set any status it likes, including
 * {@code OCCUPIED}, and so must not be reachable with the authority the lifecycle endpoints use.
 */
@RestController
@RequestMapping("/api/beds")
public class BedStatusResource {

    private static final Logger LOG = LoggerFactory.getLogger(BedStatusResource.class);

    private final BedWorkflowService bedWorkflowService;

    public BedStatusResource(BedWorkflowService bedWorkflowService) {
        this.bedWorkflowService = bedWorkflowService;
    }

    /**
     * {@code PUT /beds/:bedId/available} : this bed is clean and empty and may be given to the next
     * patient.
     *
     * <p>Answers {@code 200} rather than {@code 204}: the caller is told the status it replaced, which
     * is what makes a double click or a race with another nurse visible instead of silent.
     */
    @PutMapping("/{bedId}/available")
    public ResponseEntity<BedStatusViewDTO> markAvailable(@PathVariable("bedId") Long bedId) {
        LOG.debug("REST request to declare bed {} available", bedId);
        return ResponseEntity.ok(bedWorkflowService.markAvailable(bedId));
    }

    /**
     * {@code PUT /beds/:bedId/maintenance} : this bed is out of service, and here is why.
     */
    @PutMapping("/{bedId}/maintenance")
    public ResponseEntity<BedStatusViewDTO> markMaintenance(
        @PathVariable("bedId") Long bedId,
        @Valid @RequestBody MarkBedMaintenanceRequestDTO request
    ) {
        LOG.debug("REST request to take bed {} out of service", bedId);
        return ResponseEntity.ok(bedWorkflowService.markMaintenance(bedId, request));
    }
}
