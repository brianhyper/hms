package com.hyperbrains.hms.service.workflow;

import com.hyperbrains.hms.service.dto.view.ChartVitalsRequestDTO;
import com.hyperbrains.hms.service.dto.view.InpatientVitalsViewDTO;
import com.hyperbrains.hms.service.dto.view.VitalsChartingResultDTO;
import java.util.List;

/**
 * Charting observations on the ward.
 *
 * <p>Validation is not re-implemented here. Inpatient charting goes through the same
 * {@code VitalsValidator} as outpatient triage, so a pulse of 400 is refused at the bedside exactly as it
 * is at the desk, and the bounds live in one place.
 *
 * <p>What differs from triage is cardinality: outpatient vitals are one row per visit, an inpatient is
 * charted twice a day for a week. That is why a charted observation is never edited. A wrong entry is
 * corrected by charting a row that supersedes it, both rows stay on the chart, and the correction says
 * what it replaced.
 */
public interface InpatientChartingService {

    /**
     * Chart a new observation.
     *
     * <p>Always adds a row. A reading that is possible but alarming is saved and returned with its warnings;
     * a reading that is not possible is refused outright.
     *
     * @throws com.hyperbrains.hms.service.VitalsRejectedException when a reading is outside the range a
     *         living person can produce
     * @throws com.hyperbrains.hms.service.BusinessRuleViolationException when there is no such stay, the stay
     *         is over, or a correction reason was supplied to the wrong endpoint
     */
    VitalsChartingResultDTO chart(Long admissionId, ChartVitalsRequestDTO request);

    /**
     * Supersede an observation that was charted wrongly.
     *
     * <p>The original is kept and the correcting row points at it, so the chart shows what was recorded and
     * what replaced it. A reason is mandatory: a clinical record that can be altered without one is a
     * record that cannot be trusted.
     */
    VitalsChartingResultDTO correct(Long admissionId, Long vitalsId, ChartVitalsRequestDTO request);

    /**
     * This stay's chart, oldest first, with each row saying whether a later one replaced it.
     *
     * <p>Filtered by the inpatient access rule, because a chart reached by an admission id would otherwise
     * hand any doctor any patient.
     */
    List<InpatientVitalsViewDTO> chartOf(Long admissionId);
}
