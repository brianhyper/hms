package com.hyperbrains.hms.service.dto.view;

import com.hyperbrains.hms.domain.enumeration.BedStatus;
import java.time.Instant;

/**
 * A bed after its status was changed by hand.
 *
 * <p>Carries the status it came from as well as the one it reached, so a screen can say what actually
 * happened ("CLEANING → AVAILABLE") without having to have captured the previous state itself, and so
 * a client that raced another user cannot mistake somebody else's change for its own.
 */
public record BedStatusViewDTO(
    Long bedId,
    String bedNumber,
    Long wardId,
    String wardName,
    BedStatus previousStatus,
    BedStatus status,
    Instant changedAt
) {}
