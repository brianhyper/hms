package com.hyperbrains.hms.web.rest;

import com.hyperbrains.hms.service.dto.view.AwaitingBedViewDTO;
import com.hyperbrains.hms.service.workflow.InpatientWorklistService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The inpatient worklists.
 *
 * <p>Its own set of routes because a worklist is not a filter on the CRUD list: it answers "what has to be
 * done next", in the order it has to be done, and both of those are decisions the server makes so that two
 * screens cannot make them differently.
 */
@RestController
@RequestMapping("/api/inpatient-worklist")
public class InpatientWorklistResource {

    private static final Logger LOG = LoggerFactory.getLogger(InpatientWorklistResource.class);

    private final InpatientWorklistService inpatientWorklistService;

    public InpatientWorklistResource(InpatientWorklistService inpatientWorklistService) {
        this.inpatientWorklistService = inpatientWorklistService;
    }

    /**
     * {@code GET /inpatient-worklist/awaiting-bed} : patients admitted and still without a bed.
     */
    @GetMapping("/awaiting-bed")
    public ResponseEntity<List<AwaitingBedViewDTO>> awaitingBed() {
        LOG.debug("REST request for the awaiting-bed worklist");
        return ResponseEntity.ok(inpatientWorklistService.awaitingBed());
    }
}
