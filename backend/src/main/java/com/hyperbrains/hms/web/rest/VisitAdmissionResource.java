package com.hyperbrains.hms.web.rest;

import com.hyperbrains.hms.service.dto.view.AdmitPatientRequestDTO;
import com.hyperbrains.hms.service.dto.view.VisitAdmissionResultDTO;
import com.hyperbrains.hms.service.workflow.AdmissionWorkflowService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The admit-patient action.
 *
 * <p>Separate from the generated {@code /api/visits} CRUD, which can set a visit's type by hand and
 * would therefore make this action and its guards optional. It is also separate from the consultation
 * routes because the specification allows the action during <em>or</em> after a consultation: a patient
 * who has already been sent for tests is admitted from the same visit, without a second consultation.
 */
@RestController
@RequestMapping("/api/visit-admissions")
public class VisitAdmissionResource {

    private static final Logger LOG = LoggerFactory.getLogger(VisitAdmissionResource.class);

    private final AdmissionWorkflowService admissionService;

    public VisitAdmissionResource(AdmissionWorkflowService admissionService) {
        this.admissionService = admissionService;
    }

    /**
     * {@code POST /visit-admissions/:visitId/admit} : keep this patient in.
     *
     * <p>Answers {@code 200} rather than {@code 201}: nothing new is created. The visit that comes back
     * is the same visit, with a different type.
     */
    @PostMapping("/{visitId}/admit")
    public ResponseEntity<VisitAdmissionResultDTO> admit(
        @PathVariable("visitId") Long visitId,
        @Valid @RequestBody AdmitPatientRequestDTO request
    ) {
        LOG.debug("REST request to convert visit {} into an admission", visitId);
        return ResponseEntity.ok(admissionService.admit(visitId, request));
    }
}
