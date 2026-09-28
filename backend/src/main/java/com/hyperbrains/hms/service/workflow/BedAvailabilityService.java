package com.hyperbrains.hms.service.workflow;

import com.hyperbrains.hms.service.dto.view.BedAvailabilityViewDTO;
import com.hyperbrains.hms.service.dto.view.WardOccupancyViewDTO;
import java.util.List;

/**
 * Read access to what beds exist and which of them are free.
 *
 * <p>Separate from {@link BedWorkflowService} for the same reason the queue reads are separate from
 * the services that move patients: looking at the ward must not be able to change it.
 */
public interface BedAvailabilityService {

    /**
     * The beds that could be given to a patient right now, optionally narrowed to one ward, one bed
     * type, or both. A null filter means "any".
     */
    List<BedAvailabilityViewDTO> findAssignable(Long wardId, Long bedTypeId);

    /**
     * Every ward with its capacity counted from its beds.
     *
     * <p>Includes wards with no beds, reporting zero.
     */
    List<WardOccupancyViewDTO> wardOccupancy();
}
