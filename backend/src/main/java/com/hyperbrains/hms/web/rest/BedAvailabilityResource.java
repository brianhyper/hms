package com.hyperbrains.hms.web.rest;

import com.hyperbrains.hms.service.dto.view.BedAvailabilityViewDTO;
import com.hyperbrains.hms.service.dto.view.WardOccupancyViewDTO;
import com.hyperbrains.hms.service.workflow.BedAvailabilityService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Which beds are free, and how full the wards are.
 *
 * <p>Its own pair of routes rather than a filter on {@code /api/beds}: the generated list returns every
 * bed in every state, and the question this answers — "where can this patient go" — is a different
 * question with a different rule behind it. Answering it in the client would mean the client deciding
 * what assignable means, which is how the screen and the server start disagreeing about a bed.
 */
@RestController
@RequestMapping("/api/bed-availability")
public class BedAvailabilityResource {

    private static final Logger LOG = LoggerFactory.getLogger(BedAvailabilityResource.class);

    private final BedAvailabilityService bedAvailabilityService;

    public BedAvailabilityResource(BedAvailabilityService bedAvailabilityService) {
        this.bedAvailabilityService = bedAvailabilityService;
    }

    /**
     * {@code GET /bed-availability} : the beds a patient could be put into right now.
     *
     * @param wardId     optional — only beds in this ward
     * @param bedTypeId  optional — only beds of this type
     */
    @GetMapping
    public ResponseEntity<List<BedAvailabilityViewDTO>> available(
        @RequestParam(name = "wardId", required = false) Long wardId,
        @RequestParam(name = "bedTypeId", required = false) Long bedTypeId
    ) {
        LOG.debug("REST request for assignable beds, ward {} and bed type {}", wardId, bedTypeId);
        return ResponseEntity.ok(bedAvailabilityService.findAssignable(wardId, bedTypeId));
    }

    /**
     * {@code GET /bed-availability/wards} : every ward with its capacity counted from its beds.
     */
    @GetMapping("/wards")
    public ResponseEntity<List<WardOccupancyViewDTO>> wards() {
        LOG.debug("REST request for ward occupancy");
        return ResponseEntity.ok(bedAvailabilityService.wardOccupancy());
    }
}
