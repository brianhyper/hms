package com.hyperbrains.hms.service.dto.view;

import java.time.Instant;

/**
 * One entry on the duty roster.
 *
 * <p>Carries both ends of the period, including the absent one, because "until further notice" and
 * "until 20:00" are different commitments and a roster that cannot tell them apart is one somebody will
 * misread at handover.
 */
public record WardCoverViewDTO(
    Long coverId,
    Long doctorId,
    String doctorName,
    Long wardId,
    String wardName,
    Instant coversFrom,
    Instant coversTo,
    String note,
    Long assignedById,
    String assignedByName,
    boolean inForce
) {}
