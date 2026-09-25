package com.hyperbrains.hms.web.rest;

import com.hyperbrains.hms.repository.ConsultationRepository;
import com.hyperbrains.hms.service.ConsultationService;
import com.hyperbrains.hms.service.dto.ConsultationDTO;
import com.hyperbrains.hms.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.hyperbrains.hms.domain.Consultation}.
 */
@RestController
@RequestMapping("/api/consultations")
public class ConsultationResource {

    private static final Logger LOG = LoggerFactory.getLogger(ConsultationResource.class);

    private static final String ENTITY_NAME = "consultation";

    @Value("${jhipster.clientApp.name:hms}")
    private String applicationName;

    private final ConsultationService consultationService;

    private final ConsultationRepository consultationRepository;

    public ConsultationResource(ConsultationService consultationService, ConsultationRepository consultationRepository) {
        this.consultationService = consultationService;
        this.consultationRepository = consultationRepository;
    }

    /**
     * {@code POST  /consultations} : Create a new consultation.
     *
     * @param consultationDTO the consultationDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new consultationDTO, or with status {@code 400 (Bad Request)} if the consultation has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<ConsultationDTO> createConsultation(@Valid @RequestBody ConsultationDTO consultationDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save Consultation : {}", consultationDTO);
        if (consultationDTO.getId() != null) {
            throw new BadRequestAlertException("A new consultation cannot already have an ID", ENTITY_NAME, "idexists");
        }
        consultationDTO = consultationService.save(consultationDTO);
        return ResponseEntity.created(new URI("/api/consultations/" + consultationDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, consultationDTO.getId().toString()))
            .body(consultationDTO);
    }

    /**
     * {@code PUT  /consultations/:id} : Updates an existing consultation.
     *
     * @param id the id of the consultationDTO to save.
     * @param consultationDTO the consultationDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated consultationDTO,
     * or with status {@code 400 (Bad Request)} if the consultationDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the consultationDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ConsultationDTO> updateConsultation(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody ConsultationDTO consultationDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Consultation : {}, {}", id, consultationDTO);
        if (consultationDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, consultationDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!consultationRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        consultationDTO = consultationService.update(consultationDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, consultationDTO.getId().toString()))
            .body(consultationDTO);
    }

    /**
     * {@code PATCH  /consultations/:id} : Partial updates given fields of an existing consultation, field will ignore if it is null
     *
     * @param id the id of the consultationDTO to save.
     * @param consultationDTO the consultationDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated consultationDTO,
     * or with status {@code 400 (Bad Request)} if the consultationDTO is not valid,
     * or with status {@code 404 (Not Found)} if the consultationDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the consultationDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<ConsultationDTO> partialUpdateConsultation(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody ConsultationDTO consultationDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Consultation partially : {}, {}", id, consultationDTO);
        if (consultationDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, consultationDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!consultationRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<ConsultationDTO> result = consultationService.partialUpdate(consultationDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, consultationDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /consultations} : get all the Consultations.
     *
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @param filter the filter of the request.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Consultations in body.
     */
    @GetMapping("")
    public List<ConsultationDTO> getAllConsultations(
        @RequestParam(name = "filter", required = false) String filter,
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        if ("visit-is-null".equals(filter)) {
            LOG.debug("REST request to get all Consultations where visit is null");
            return consultationService.findAllWhereVisitIsNull();
        }
        LOG.debug("REST request to get all Consultations");
        return consultationService.findAll();
    }

    /**
     * {@code GET  /consultations/:id} : get the "id" consultation.
     *
     * @param id the id of the consultationDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the consultationDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ConsultationDTO> getConsultation(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Consultation : {}", id);
        Optional<ConsultationDTO> consultationDTO = consultationService.findOne(id);
        return ResponseUtil.wrapOrNotFound(consultationDTO);
    }

    /**
     * {@code DELETE  /consultations/:id} : delete the "id" consultation.
     *
     * @param id the id of the consultationDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConsultation(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Consultation : {}", id);
        consultationService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
