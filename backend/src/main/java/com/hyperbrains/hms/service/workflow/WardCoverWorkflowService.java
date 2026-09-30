package com.hyperbrains.hms.service.workflow;

import com.hyperbrains.hms.service.dto.view.AssignWardCoverRequestDTO;
import com.hyperbrains.hms.service.dto.view.WardCoverViewDTO;
import java.util.List;

/**
 * The duty roster: which doctor is responsible for which ward, and when.
 *
 * <p>This exists because the inpatient access rule has a half that nothing in Phase 1 could answer — "a
 * doctor also sees the patients on a ward they are covering" needs somebody to have recorded who covers
 * what. Without the roster that clause is either unimplementable or has to be invented per request.
 *
 * <p>The generated {@code /api/ward-covers} CRUD is closed to Super Admin, which is also who owns the
 * roster; these actions exist so the two things that can go wrong in a period of cover are refused rather
 * than stored: a period that ends before it starts, and a period naming somebody who is not a doctor.
 */
public interface WardCoverWorkflowService {

    /**
     * Put a doctor on duty for a ward, from a moment until an optional moment.
     *
     * <p>An absent end is "until further notice", which is a real arrangement and not the same as a period
     * that has expired.
     */
    WardCoverViewDTO assign(AssignWardCoverRequestDTO request);

    /**
     * End a period of cover early, at the moment it is ended.
     *
     * <p>Which is how a shift that is cut short is recorded honestly: the roster keeps the period that was
     * planned and the moment it actually stopped.
     */
    WardCoverViewDTO end(Long coverId);

    /** The cover in force at this moment, ward by ward. */
    List<WardCoverViewDTO> current();
}
