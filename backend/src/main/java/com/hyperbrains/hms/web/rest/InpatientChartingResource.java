package com.hyperbrains.hms.web.rest;

import com.hyperbrains.hms.service.dto.view.ChartVitalsRequestDTO;
import com.hyperbrains.hms.service.dto.view.InpatientVitalsViewDTO;
import com.hyperbrains.hms.service.dto.view.VitalsChartingResultDTO;
import com.hyperbrains.hms.service.workflow.InpatientChartingService;
import jakarta.validation.Valid;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Charting observations on a stay, and reading the chart back.
 *
 * <p>Named {@code InpatientChartingResource} rather than {@code InpatientVitalsResource}, which is the
 * generated CRUD resource for the same entity: regenerating that entity cannot overwrite these actions.
 * The generated CRUD is restricted to Super Admin, because as raw CRUD it can write a reading straight into
 * the chart without going through the validator.
 */
@RestController
@RequestMapping("/api/inpatient-charting")
public class InpatientChartingResource {

    private static final Logger LOG = LoggerFactory.getLogger(InpatientChartingResource.class);

    private final InpatientChartingService chartingService;

    public InpatientChartingResource(InpatientChartingService chartingService) {
        this.chartingService = chartingService;
    }

    /**
     * {@code POST /inpatient-charting/:admissionId/vitals} : chart an observation.
     *
     * <p>Answers {@code 200} rather than {@code 201} even though a row is created, because the caller is
     * being answered with the reading's verdict — saved, and here is what looks wrong with it — rather than
     * with a new resource's address.
     */
    @PostMapping("/{admissionId}/vitals")
    public ResponseEntity<VitalsChartingResultDTO> chart(
        @PathVariable("admissionId") Long admissionId,
        @Valid @RequestBody ChartVitalsRequestDTO request
    ) {
        LOG.debug("REST request to chart vitals for admission {}", admissionId);
        return ResponseEntity.ok(chartingService.chart(admissionId, request));
    }

    /**
     * {@code POST /inpatient-charting/:admissionId/vitals/:vitalsId/corrections} : replace an observation
     * that was charted wrongly, with a reason.
     */
    @PostMapping("/{admissionId}/vitals/{vitalsId}/corrections")
    public ResponseEntity<VitalsChartingResultDTO> correct(
        @PathVariable("admissionId") Long admissionId,
        @PathVariable("vitalsId") Long vitalsId,
        @Valid @RequestBody ChartVitalsRequestDTO request
    ) {
        LOG.debug("REST request to correct observation {} on admission {}", vitalsId, admissionId);
        return ResponseEntity.ok(chartingService.correct(admissionId, vitalsId, request));
    }

    /**
     * {@code GET /inpatient-charting/:admissionId/vitals} : the chart, oldest first.
     *
     * <p>This is what a doctor reads on the dashboard under My Patients, so it goes through the inpatient
     * access rule rather than being open to any doctor who knows an admission id.
     */
    @GetMapping("/{admissionId}/vitals")
    public ResponseEntity<List<InpatientVitalsViewDTO>> chart(@PathVariable("admissionId") Long admissionId) {
        LOG.debug("REST request for the chart of admission {}", admissionId);
        return ResponseEntity.ok(chartingService.chartOf(admissionId));
    }
}
