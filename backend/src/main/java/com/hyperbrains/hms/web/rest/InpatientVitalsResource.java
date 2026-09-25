package com.hyperbrains.hms.web.rest;

import com.hyperbrains.hms.repository.InpatientVitalsRepository;
import com.hyperbrains.hms.service.InpatientVitalsService;
import com.hyperbrains.hms.service.dto.InpatientVitalsDTO;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.hyperbrains.hms.domain.InpatientVitals}.
 */
@RestController
@RequestMapping("/api/inpatient-vitals")
public class InpatientVitalsResource {

    private static final Logger LOG = LoggerFactory.getLogger(InpatientVitalsResource.class);

    private static final String ENTITY_NAME = "inpatientVitals";

    @Value("${jhipster.clientApp.name:hms}")
    private String applicationName;

    private final InpatientVitalsService inpatientVitalsService;

    private final InpatientVitalsRepository inpatientVitalsRepository;

    public InpatientVitalsResource(InpatientVitalsService inpatientVitalsService, InpatientVitalsRepository inpatientVitalsRepository) {
        this.inpatientVitalsService = inpatientVitalsService;
        this.inpatientVitalsRepository = inpatientVitalsRepository;
    }

    /**
     * {@code POST  /inpatient-vitals} : Create a new inpatientVitals.
     *
     * @param inpatientVitalsDTO the inpatientVitalsDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new inpatientVitalsDTO, or with status {@code 400 (Bad Request)} if the inpatientVitals has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<InpatientVitalsDTO> createInpatientVitals(@Valid @RequestBody InpatientVitalsDTO inpatientVitalsDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save InpatientVitals : {}", inpatientVitalsDTO);
        if (inpatientVitalsDTO.getId() != null) {
            throw new BadRequestAlertException("A new inpatientVitals cannot already have an ID", ENTITY_NAME, "idexists");
        }
        inpatientVitalsDTO = inpatientVitalsService.save(inpatientVitalsDTO);
        return ResponseEntity.created(new URI("/api/inpatient-vitals/" + inpatientVitalsDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, inpatientVitalsDTO.getId().toString()))
            .body(inpatientVitalsDTO);
    }

    /**
     * {@code PUT  /inpatient-vitals/:id} : Updates an existing inpatientVitals.
     *
     * @param id the id of the inpatientVitalsDTO to save.
     * @param inpatientVitalsDTO the inpatientVitalsDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated inpatientVitalsDTO,
     * or with status {@code 400 (Bad Request)} if the inpatientVitalsDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the inpatientVitalsDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<InpatientVitalsDTO> updateInpatientVitals(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody InpatientVitalsDTO inpatientVitalsDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update InpatientVitals : {}, {}", id, inpatientVitalsDTO);
        if (inpatientVitalsDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, inpatientVitalsDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!inpatientVitalsRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        inpatientVitalsDTO = inpatientVitalsService.update(inpatientVitalsDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, inpatientVitalsDTO.getId().toString()))
            .body(inpatientVitalsDTO);
    }

    /**
     * {@code PATCH  /inpatient-vitals/:id} : Partial updates given fields of an existing inpatientVitals, field will ignore if it is null
     *
     * @param id the id of the inpatientVitalsDTO to save.
     * @param inpatientVitalsDTO the inpatientVitalsDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated inpatientVitalsDTO,
     * or with status {@code 400 (Bad Request)} if the inpatientVitalsDTO is not valid,
     * or with status {@code 404 (Not Found)} if the inpatientVitalsDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the inpatientVitalsDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<InpatientVitalsDTO> partialUpdateInpatientVitals(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody InpatientVitalsDTO inpatientVitalsDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update InpatientVitals partially : {}, {}", id, inpatientVitalsDTO);
        if (inpatientVitalsDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, inpatientVitalsDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!inpatientVitalsRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<InpatientVitalsDTO> result = inpatientVitalsService.partialUpdate(inpatientVitalsDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, inpatientVitalsDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /inpatient-vitals} : get all the Inpatient Vitals.
     *
     * @param pageable the pagination information.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Inpatient Vitals in body.
     */
    @GetMapping("")
    public ResponseEntity<List<InpatientVitalsDTO>> getAllInpatientVitalses(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to get a page of InpatientVitalses");
        Page<InpatientVitalsDTO> page;
        if (eagerload) {
            page = inpatientVitalsService.findAllWithEagerRelationships(pageable);
        } else {
            page = inpatientVitalsService.findAll(pageable);
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /inpatient-vitals/:id} : get the "id" inpatientVitals.
     *
     * @param id the id of the inpatientVitalsDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the inpatientVitalsDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<InpatientVitalsDTO> getInpatientVitals(@PathVariable("id") Long id) {
        LOG.debug("REST request to get InpatientVitals : {}", id);
        Optional<InpatientVitalsDTO> inpatientVitalsDTO = inpatientVitalsService.findOne(id);
        return ResponseUtil.wrapOrNotFound(inpatientVitalsDTO);
    }

    /**
     * {@code DELETE  /inpatient-vitals/:id} : delete the "id" inpatientVitals.
     *
     * @param id the id of the inpatientVitalsDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInpatientVitals(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete InpatientVitals : {}", id);
        inpatientVitalsService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
