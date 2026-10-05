package com.hyperbrains.hms.web.rest;

import com.hyperbrains.hms.service.PatientAccessLogService;
import com.hyperbrains.hms.service.dto.view.PatientAccessLogViewDTO;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.PaginationUtil;

/**
 * Reading the patient access log.
 *
 * <p>Read-only, and there is no route that writes: the entries are created by opening a chart, which is the event
 * being recorded, and nothing else may add one. Administration reads it, Super Admin reads it, and no other role can
 * — the log itself is a list of who looked at whom, so it is not something a clinician has any reason to browse.
 *
 * <p>A read is always scoped to one patient rather than paged over the whole hospital. The question this answers is
 * "who has been looking at this record", and an unscoped listing would answer "who looked at anything", which is a
 * different and much larger disclosure.
 */
@RestController
@RequestMapping("/api/patient-access-logs")
public class PatientAccessLogResource {

    private static final Logger LOG = LoggerFactory.getLogger(PatientAccessLogResource.class);

    private final PatientAccessLogService patientAccessLogService;

    public PatientAccessLogResource(PatientAccessLogService patientAccessLogService) {
        this.patientAccessLogService = patientAccessLogService;
    }

    /**
     * {@code GET /patient-access-logs?patientId=} : who has opened this patient's chart, most recent first.
     *
     * @param patientId the patient whose log is being read.
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the entries in the body.
     */
    @GetMapping("")
    public ResponseEntity<List<PatientAccessLogViewDTO>> getAccessLog(
        @RequestParam("patientId") Long patientId,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to read the access log of patient {}", patientId);
        Page<PatientAccessLogViewDTO> page = patientAccessLogService.findByPatient(patientId, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }
}
