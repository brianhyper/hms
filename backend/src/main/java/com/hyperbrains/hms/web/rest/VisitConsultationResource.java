package com.hyperbrains.hms.web.rest;

import com.hyperbrains.hms.service.dto.ConsultationDTO;
import com.hyperbrains.hms.service.dto.view.AddConsultationAddendumRequestDTO;
import com.hyperbrains.hms.service.dto.view.ConsultationAddendumDTO;
import com.hyperbrains.hms.service.dto.view.StartConsultationRequestDTO;
import com.hyperbrains.hms.service.dto.view.UpdateConsultationRequestDTO;
import com.hyperbrains.hms.service.workflow.ConsultationWorkflowService;
import jakarta.validation.Valid;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Consultation actions.
 *
 * <p>Separate from the generated {@code /api/consultations} CRUD, which is kept for reads. A
 * consultation is not a record a client posts: opening one claims the patient, and completing one
 * generates a charge and moves the visit.
 *
 * <p>The two correction rules are visible in the shape of this API. {@code PUT .../notes} is an
 * in-place edit and stops working once the consultation is complete; {@code POST .../addenda} is the
 * only way to add anything afterwards, and it appends without touching the original.
 */
@RestController
@RequestMapping("/api/visit-consultation")
public class VisitConsultationResource {

    private static final Logger LOG = LoggerFactory.getLogger(VisitConsultationResource.class);

    private final ConsultationWorkflowService consultationService;

    public VisitConsultationResource(ConsultationWorkflowService consultationService) {
        this.consultationService = consultationService;
    }

    /** {@code POST /visit-consultation/:visitId/start} : claim this patient from the queue. */
    @PostMapping("/{visitId}/start")
    public ResponseEntity<ConsultationDTO> start(
        @PathVariable("visitId") Long visitId,
        @Valid @RequestBody StartConsultationRequestDTO request
    ) {
        LOG.debug("REST request to start a consultation for visit {}", visitId);
        return ResponseEntity.status(HttpStatus.CREATED).body(consultationService.start(visitId, request));
    }

    /**
     * {@code PUT /visit-consultation/:consultationId/notes} : save notes while in progress.
     *
     * <p>Returns {@code 409} once the consultation is complete — an edit at that point is refused
     * rather than silently overwriting a record that has already been acted on.
     */
    @PutMapping("/{consultationId}/notes")
    public ResponseEntity<ConsultationDTO> updateNotes(
        @PathVariable("consultationId") Long consultationId,
        @Valid @RequestBody UpdateConsultationRequestDTO request
    ) {
        LOG.debug("REST request to update consultation {}", consultationId);
        return ResponseEntity.ok(consultationService.updateNotes(consultationId, request));
    }

    /** {@code POST /visit-consultation/:consultationId/complete} : finish and charge for it. */
    @PostMapping("/{consultationId}/complete")
    public ResponseEntity<ConsultationDTO> complete(
        @PathVariable("consultationId") Long consultationId,
        @Valid @RequestBody UpdateConsultationRequestDTO request
    ) {
        LOG.debug("REST request to complete consultation {}", consultationId);
        return ResponseEntity.ok(consultationService.complete(consultationId, request));
    }

    /** {@code GET /visit-consultation/:consultationId/addenda} : notes appended after completion. */
    @GetMapping("/{consultationId}/addenda")
    public ResponseEntity<List<ConsultationAddendumDTO>> addenda(@PathVariable("consultationId") Long consultationId) {
        LOG.debug("REST request to list addenda for consultation {}", consultationId);
        return ResponseEntity.ok(consultationService.listAddenda(consultationId));
    }

    /** {@code POST /visit-consultation/:consultationId/addenda} : append a note. */
    @PostMapping("/{consultationId}/addenda")
    public ResponseEntity<ConsultationAddendumDTO> addAddendum(
        @PathVariable("consultationId") Long consultationId,
        @Valid @RequestBody AddConsultationAddendumRequestDTO request
    ) {
        LOG.debug("REST request to add an addendum to consultation {}", consultationId);
        return ResponseEntity.status(HttpStatus.CREATED).body(consultationService.addAddendum(consultationId, request));
    }
}
