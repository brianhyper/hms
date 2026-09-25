package com.hyperbrains.hms.web.rest;

import com.hyperbrains.hms.service.dto.VisitDTO;
import com.hyperbrains.hms.service.dto.view.StartVitalsRequestDTO;
import com.hyperbrains.hms.service.dto.view.VitalsSubmissionRequestDTO;
import com.hyperbrains.hms.service.dto.view.VitalsSubmissionResultDTO;
import com.hyperbrains.hms.service.workflow.TriageService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Triage actions.
 *
 * <p>{@code POST /visit-triage/:visitId/vitals} returns {@code 200} with the saved reading and any
 * warnings, or {@code 400} with per-field errors when a value is not physiologically possible. Those
 * two responses are the two-tier rule made visible: one saves a possible-but-abnormal reading and
 * says so, the other refuses an impossible one.
 */
@RestController
@RequestMapping("/api/visit-triage")
public class TriageResource {

    private static final Logger LOG = LoggerFactory.getLogger(TriageResource.class);

    private final TriageService triageService;

    public TriageResource(TriageService triageService) {
        this.triageService = triageService;
    }

    /** {@code POST /visit-triage/:visitId/start} : take this patient from the vitals queue. */
    @PostMapping("/{visitId}/start")
    public ResponseEntity<VisitDTO> startVitals(
        @PathVariable("visitId") Long visitId,
        @Valid @RequestBody StartVitalsRequestDTO request
    ) {
        LOG.debug("REST request to start vitals for visit {}", visitId);
        return ResponseEntity.ok(triageService.startVitals(visitId, request));
    }

    /**
     * {@code POST /visit-triage/:visitId/vitals} : record vitals and send the patient to the doctor.
     *
     * <p>{@code 201} the first time, and also {@code 201} when correcting — the response body's
     * {@code correction} flag distinguishes them, so the client does not have to infer it from a
     * status code that would otherwise mean something else.
     */
    @PostMapping("/{visitId}/vitals")
    public ResponseEntity<VitalsSubmissionResultDTO> submitVitals(
        @PathVariable("visitId") Long visitId,
        @Valid @RequestBody VitalsSubmissionRequestDTO request
    ) {
        LOG.debug("REST request to record vitals for visit {}", visitId);
        return ResponseEntity.status(HttpStatus.CREATED).body(triageService.submitVitals(visitId, request));
    }
}
