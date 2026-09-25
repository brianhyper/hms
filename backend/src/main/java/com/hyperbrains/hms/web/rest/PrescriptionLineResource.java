package com.hyperbrains.hms.web.rest;

import com.hyperbrains.hms.repository.PrescriptionLineRepository;
import com.hyperbrains.hms.service.PrescriptionLineService;
import com.hyperbrains.hms.service.dto.PrescriptionLineDTO;
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
 * REST controller for managing {@link com.hyperbrains.hms.domain.PrescriptionLine}.
 */
@RestController
@RequestMapping("/api/prescription-lines")
public class PrescriptionLineResource {

    private static final Logger LOG = LoggerFactory.getLogger(PrescriptionLineResource.class);

    private static final String ENTITY_NAME = "prescriptionLine";

    @Value("${jhipster.clientApp.name:hms}")
    private String applicationName;

    private final PrescriptionLineService prescriptionLineService;

    private final PrescriptionLineRepository prescriptionLineRepository;

    public PrescriptionLineResource(
        PrescriptionLineService prescriptionLineService,
        PrescriptionLineRepository prescriptionLineRepository
    ) {
        this.prescriptionLineService = prescriptionLineService;
        this.prescriptionLineRepository = prescriptionLineRepository;
    }

    /**
     * {@code POST  /prescription-lines} : Create a new prescriptionLine.
     *
     * @param prescriptionLineDTO the prescriptionLineDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new prescriptionLineDTO, or with status {@code 400 (Bad Request)} if the prescriptionLine has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<PrescriptionLineDTO> createPrescriptionLine(@Valid @RequestBody PrescriptionLineDTO prescriptionLineDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save PrescriptionLine : {}", prescriptionLineDTO);
        if (prescriptionLineDTO.getId() != null) {
            throw new BadRequestAlertException("A new prescriptionLine cannot already have an ID", ENTITY_NAME, "idexists");
        }
        prescriptionLineDTO = prescriptionLineService.save(prescriptionLineDTO);
        return ResponseEntity.created(new URI("/api/prescription-lines/" + prescriptionLineDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, prescriptionLineDTO.getId().toString()))
            .body(prescriptionLineDTO);
    }

    /**
     * {@code PUT  /prescription-lines/:id} : Updates an existing prescriptionLine.
     *
     * @param id the id of the prescriptionLineDTO to save.
     * @param prescriptionLineDTO the prescriptionLineDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated prescriptionLineDTO,
     * or with status {@code 400 (Bad Request)} if the prescriptionLineDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the prescriptionLineDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<PrescriptionLineDTO> updatePrescriptionLine(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody PrescriptionLineDTO prescriptionLineDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update PrescriptionLine : {}, {}", id, prescriptionLineDTO);
        if (prescriptionLineDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, prescriptionLineDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!prescriptionLineRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        prescriptionLineDTO = prescriptionLineService.update(prescriptionLineDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, prescriptionLineDTO.getId().toString()))
            .body(prescriptionLineDTO);
    }

    /**
     * {@code PATCH  /prescription-lines/:id} : Partial updates given fields of an existing prescriptionLine, field will ignore if it is null
     *
     * @param id the id of the prescriptionLineDTO to save.
     * @param prescriptionLineDTO the prescriptionLineDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated prescriptionLineDTO,
     * or with status {@code 400 (Bad Request)} if the prescriptionLineDTO is not valid,
     * or with status {@code 404 (Not Found)} if the prescriptionLineDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the prescriptionLineDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<PrescriptionLineDTO> partialUpdatePrescriptionLine(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody PrescriptionLineDTO prescriptionLineDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update PrescriptionLine partially : {}, {}", id, prescriptionLineDTO);
        if (prescriptionLineDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, prescriptionLineDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!prescriptionLineRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<PrescriptionLineDTO> result = prescriptionLineService.partialUpdate(prescriptionLineDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, prescriptionLineDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /prescription-lines} : get all the Prescription Lines.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Prescription Lines in body.
     */
    @GetMapping("")
    public List<PrescriptionLineDTO> getAllPrescriptionLines() {
        LOG.debug("REST request to get all PrescriptionLines");
        return prescriptionLineService.findAll();
    }

    /**
     * {@code GET  /prescription-lines/:id} : get the "id" prescriptionLine.
     *
     * @param id the id of the prescriptionLineDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the prescriptionLineDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PrescriptionLineDTO> getPrescriptionLine(@PathVariable("id") Long id) {
        LOG.debug("REST request to get PrescriptionLine : {}", id);
        Optional<PrescriptionLineDTO> prescriptionLineDTO = prescriptionLineService.findOne(id);
        return ResponseUtil.wrapOrNotFound(prescriptionLineDTO);
    }

    /**
     * {@code DELETE  /prescription-lines/:id} : delete the "id" prescriptionLine.
     *
     * @param id the id of the prescriptionLineDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePrescriptionLine(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete PrescriptionLine : {}", id);
        prescriptionLineService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
