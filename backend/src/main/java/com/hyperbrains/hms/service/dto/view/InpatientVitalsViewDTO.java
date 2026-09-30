package com.hyperbrains.hms.service.dto.view;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * One charted observation.
 *
 * <p>{@code supersededBy} is what makes this a clinical record rather than a log of keystrokes: a wrong
 * entry is never overwritten, it is superseded by a correcting row, and both stay on the chart with the
 * correction naming what it replaced. A reader can therefore always tell which figure is the one in force
 * without having to compare timestamps and guess.
 */
public record InpatientVitalsViewDTO(
    Long id,
    Long admissionId,
    Instant recordedAt,
    BigDecimal temperature,
    Integer pulseRate,
    Integer systolicBp,
    Integer diastolicBp,
    Integer oxygenSaturation,
    BigDecimal weight,
    BigDecimal height,
    BigDecimal bmi,
    String notes,
    String recordedBy,
    Long correctsId,
    String correctionReason,
    Long supersededById
) {
    /** True when a later row has replaced this observation. */
    public boolean isSuperseded() {
        return supersededById != null;
    }
}
