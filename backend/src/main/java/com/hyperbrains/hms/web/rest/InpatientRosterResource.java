package com.hyperbrains.hms.web.rest;

import com.hyperbrains.hms.service.ShiftService;
import com.hyperbrains.hms.service.dto.view.ShiftViewDTO;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

/**
 * Who is on duty, as a ward reads it.
 *
 * <p>Two reads, and no writes: the roster is written on {@code /api/shifts}, which is HR's and the super-admin's
 * escape hatch, and this is the question the ward actually asks. The separation is the point rather than an accident
 * of routing. A shift row names its person by staff record id and the staff file behind it is HR's, so the ward gets
 * this instead — the same shift, reduced to a name, a department, a time and a ward, with no field in the shape to
 * leak anything else.
 *
 * <p>Once the access rule is moved onto the roster (the next step of this gate), "is this doctor covering this ward"
 * stops being a second query over {@code WardCover} and becomes this query with the caller's own login in it.
 */
@RestController
@RequestMapping("/api/roster")
public class InpatientRosterResource {

    private static final Logger LOG = LoggerFactory.getLogger(InpatientRosterResource.class);

    private final ShiftService shiftService;

    public InpatientRosterResource(ShiftService shiftService) {
        this.shiftService = shiftService;
    }

    /**
     * {@code GET  /roster/on-duty} : who is on duty right now, across the hospital.
     *
     * @return the shifts covering this moment.
     */
    @GetMapping("/on-duty")
    public List<ShiftViewDTO> getOnDuty() {
        LOG.debug("REST request to get who is on duty now");
        return shiftService.onDutyNow();
    }

    /**
     * {@code GET  /roster/wards/:wardId/on-duty} : who is on duty on one ward right now.
     *
     * @param wardId the ward to ask about.
     * @return the shifts covering this moment on that ward, empty when nobody is. A ward that does not exist is empty
     *         too: the question is "who is on duty there", and the answer for a ward with no roster is nobody.
     */
    @GetMapping("/wards/{wardId}/on-duty")
    public List<ShiftViewDTO> getOnDutyInWard(@PathVariable("wardId") Long wardId) {
        LOG.debug("REST request to get who is on duty now in ward {}", wardId);
        return shiftService.onDutyNowInWard(wardId);
    }
}
