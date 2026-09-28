package com.hyperbrains.hms.service.workflow;

import com.hyperbrains.hms.service.dto.view.AwaitingBedViewDTO;
import java.util.List;

/**
 * Read access to the inpatient worklists.
 *
 * <p>Separate from the services that move patients, for the same reason the outpatient queues are: looking
 * at a ward's work must not be able to change it.
 */
public interface InpatientWorklistService {

    /**
     * The patients who have been admitted and have no bed yet, longest wait first.
     *
     * <p>This exists because {@code PENDING_BED} would otherwise be a state patients disappear into: the
     * doctor has decided, the patient is on no ward's list, and nobody's screen shows them. The beds they
     * could go into are <em>not</em> in this answer — they come from the availability query, which reads the
     * bed's own state, and duplicating that here would create a second answer to the same question.
     */
    List<AwaitingBedViewDTO> awaitingBed();
}
