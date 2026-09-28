package com.hyperbrains.hms.service.workflow;

import com.hyperbrains.hms.service.dto.view.BedStatusViewDTO;
import com.hyperbrains.hms.service.dto.view.MarkBedMaintenanceRequestDTO;

/**
 * The two things staff may say about a bed by hand.
 *
 * <p>Kept apart from the generated {@code /api/beds} CRUD, which can set a bed's status to any value at
 * all — {@code OCCUPIED} included — and would therefore make the whole lifecycle below optional. The
 * CRUD stays for managing beds themselves: creating, renumbering, re-pricing. Both are reachable, and
 * they are not the same door.
 *
 * <p>Who may call these is decided in {@code SecurityConfiguration}'s RBAC table, not here: the
 * endpoints carry no annotations.
 */
public interface BedWorkflowService {

    /**
     * Declares a bed clean and empty and fit for the next patient: {@code CLEANING} or
     * {@code MAINTENANCE} → {@code AVAILABLE}.
     *
     * <p>This exists because Phase 1's roles have no housekeeping in them. Without it a bed left in
     * {@code CLEANING} is invisible work — nobody can say it is ready, so it never comes back.
     */
    BedStatusViewDTO markAvailable(Long bedId);

    /**
     * Takes a bed out of service: {@code AVAILABLE} or {@code CLEANING} → {@code MAINTENANCE}.
     *
     * <p>Without this the {@code MAINTENANCE} status would exist and be unreachable, so a broken bed
     * could only be taken out of service by editing the CRUD row — which is exactly the back door this
     * service is meant to close.
     */
    BedStatusViewDTO markMaintenance(Long bedId, MarkBedMaintenanceRequestDTO request);
}
