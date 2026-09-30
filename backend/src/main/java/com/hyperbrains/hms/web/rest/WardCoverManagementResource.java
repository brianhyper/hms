package com.hyperbrains.hms.web.rest;

import com.hyperbrains.hms.service.dto.view.AssignWardCoverRequestDTO;
import com.hyperbrains.hms.service.dto.view.WardCoverViewDTO;
import com.hyperbrains.hms.service.workflow.WardCoverWorkflowService;
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
 * Managing the duty roster.
 *
 * <p>On its own base path rather than under {@code /api/ward-covers}, because the generated CRUD for the same
 * entity already owns that path and its {@code GET /{id}} route swallows any single segment — a
 * {@code /current} there is an ambiguous mapping, which fails the whole application context rather than one
 * request. It also means regenerating that entity cannot overwrite these actions.
 */
@RestController
@RequestMapping("/api/ward-cover-roster")
public class WardCoverManagementResource {

    private static final Logger LOG = LoggerFactory.getLogger(WardCoverManagementResource.class);

    private final WardCoverWorkflowService wardCoverService;

    public WardCoverManagementResource(WardCoverWorkflowService wardCoverService) {
        this.wardCoverService = wardCoverService;
    }

    /** {@code POST /ward-covers} : put a doctor on duty for a ward. */
    @PostMapping
    public ResponseEntity<WardCoverViewDTO> assign(@Valid @RequestBody AssignWardCoverRequestDTO request) {
        LOG.debug("REST request to put doctor {} on duty for ward {}", request.getDoctorId(), request.getWardId());
        return ResponseEntity.ok(wardCoverService.assign(request));
    }

    /**
     * {@code PUT /ward-covers/:coverId/end} : this cover stops now.
     *
     * <p>The moment it stopped is the server's, not the request's: a client clock is not evidence of when a
     * doctor came off duty.
     */
    @PutMapping("/{coverId}/end")
    public ResponseEntity<WardCoverViewDTO> end(@PathVariable("coverId") Long coverId) {
        LOG.debug("REST request to end ward cover {}", coverId);
        return ResponseEntity.ok(wardCoverService.end(coverId));
    }

    /** {@code GET /ward-covers/current} : who is covering which ward at this moment. */
    @GetMapping("/current")
    public ResponseEntity<List<WardCoverViewDTO>> current() {
        LOG.debug("REST request for the cover in force");
        return ResponseEntity.ok(wardCoverService.current());
    }
}
