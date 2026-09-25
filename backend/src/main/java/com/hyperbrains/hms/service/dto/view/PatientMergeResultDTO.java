package com.hyperbrains.hms.service.dto.view;

import com.hyperbrains.hms.domain.enumeration.RegistrationStatus;
import java.time.Instant;

/**
 * What a merge did.
 *
 * <p>Reports the counts rather than just succeeding, because a merge silently moves clinical history
 * between two identities and the person doing it should be able to see that it moved what they expected.
 *
 * <p>{@code openVisitsMoved} is called out separately: if a patient was mid-encounter under both
 * identities, the merged record now has two open visits, and that is worth somebody noticing at the moment
 * it happens rather than when a bill is queried.
 */
public record PatientMergeResultDTO(
    Long sourcePatientId,
    String sourceHospitalId,
    RegistrationStatus sourceStatus,
    Long targetPatientId,
    String targetHospitalId,
    int visitsMoved,
    int appointmentsMoved,
    int openVisitsMoved,
    String reason,
    Instant mergedAt
) {}
