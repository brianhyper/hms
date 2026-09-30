package com.hyperbrains.hms.service.dto.view;

import java.util.List;

/**
 * What happened when an observation was charted.
 *
 * <p>Carries {@code warnings} on a <em>successful</em> response, which is the whole shape of the two-tier
 * rule: a reading that is possible but alarming is saved and reported here, while a reading that is not
 * possible is refused outright with a 400 and never reaches this type. The warning carries the value and
 * the normal band so the nurse is told what is wrong rather than merely that something is.
 */
public record VitalsChartingResultDTO(InpatientVitalsViewDTO vitals, boolean correction, List<VitalsWarningDTO> warnings) {}
