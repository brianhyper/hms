package com.hyperbrains.hms.web.rest;

import com.hyperbrains.hms.domain.enumeration.VisitType;
import com.hyperbrains.hms.service.dto.VisitDTO;
import com.hyperbrains.hms.service.dto.view.AppointmentCheckInRequestDTO;
import com.hyperbrains.hms.service.dto.view.VisitIntakeRequestDTO;
import com.hyperbrains.hms.service.workflow.VisitIntakeService;
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
 * Action endpoints that open a visit.
 *
 * <p>These replace the generated {@code POST /api/visits}, which is kept only for its read paths.
 * A visit is not a generic record a client may post: opening one has to set a status that puts the
 * patient in the right queue, and may have to close out the appointment it came from.
 *
 * <p>Authorization is declared in the {@code PHASE 1 RBAC TABLE} of
 * {@link com.hyperbrains.hms.config.SecurityConfiguration}.
 */
@RestController
@RequestMapping("/api/visit-intake")
public class VisitIntakeResource {

    private static final Logger LOG = LoggerFactory.getLogger(VisitIntakeResource.class);

    private final VisitIntakeService visitIntakeService;

    public VisitIntakeResource(VisitIntakeService visitIntakeService) {
        this.visitIntakeService = visitIntakeService;
    }

    /**
     * {@code POST /visit-intake/check-in/:appointmentId} : the patient has arrived.
     *
     * <p>Returns {@code 409 Conflict} if the appointment is no longer scheduled — a no-show stays
     * written off, and the patient is registered as a walk-in instead.
     */
    @PostMapping("/check-in/{appointmentId}")
    public ResponseEntity<VisitDTO> checkIn(
        @PathVariable("appointmentId") Long appointmentId,
        @Valid @RequestBody AppointmentCheckInRequestDTO request
    ) {
        LOG.debug("REST request to check in appointment {}", appointmentId);
        return ResponseEntity.status(HttpStatus.CREATED).body(visitIntakeService.checkIn(appointmentId, request));
    }

    /**
     * {@code POST /visit-intake/open} : a walk-in or emergency arrival with no usable appointment.
     */
    @PostMapping("/open")
    public ResponseEntity<VisitDTO> open(@Valid @RequestBody VisitIntakeRequestDTO request) {
        LOG.debug("REST request to open a {} visit", request.getType());
        return ResponseEntity.status(HttpStatus.CREATED).body(visitIntakeService.createVisit(request));
    }

    /**
     * {@code POST /visit-intake/pharmacy-only} : collecting medication only.
     *
     * <p>The type is fixed by the endpoint. Any value the client sends is overwritten, because this
     * path exists to create one kind of visit and silently doing something else would be worse than
     * ignoring the field.
     */
    @PostMapping("/pharmacy-only")
    public ResponseEntity<VisitDTO> openPharmacyOnly(@Valid @RequestBody VisitIntakeRequestDTO request) {
        LOG.debug("REST request to open a pharmacy-only visit for patient {}", request.getPatientId());
        request.setType(VisitType.PHARMACY_ONLY);
        return ResponseEntity.status(HttpStatus.CREATED).body(visitIntakeService.createVisit(request));
    }
}
