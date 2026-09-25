package com.hyperbrains.hms.web.rest;

import com.hyperbrains.hms.repository.DiagnosisRepository;
import com.hyperbrains.hms.service.DiagnosisService;
import com.hyperbrains.hms.service.dto.DiagnosisDTO;
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
 * REST controller for managing {@link com.hyperbrains.hms.domain.Diagnosis}.
 */
@RestController
@RequestMapping("/api/diagnoses")
public class DiagnosisResource {

    private static final Logger LOG = LoggerFactory.getLogger(DiagnosisResource.class);

    private static final String ENTITY_NAME = "diagnosis";

    @Value("${jhipster.clientApp.name:hms}")
    private String applicationName;

    private final DiagnosisService diagnosisService;

    private final DiagnosisRepository diagnosisRepository;

    public DiagnosisResource(DiagnosisService diagnosisService, DiagnosisRepository diagnosisRepository) {
        this.diagnosisService = diagnosisService;
        this.diagnosisRepository = diagnosisRepository;
    }

    /**
     * {@code POST  /diagnoses} : Create a new diagnosis.
     *
     * @param diagnosisDTO the diagnosisDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new diagnosisDTO, or with status {@code 400 (Bad Request)} if the diagnosis has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<DiagnosisDTO> createDiagnosis(@Valid @RequestBody DiagnosisDTO diagnosisDTO) throws URISyntaxException {
        LOG.debug("REST request to save Diagnosis : {}", diagnosisDTO);
        if (diagnosisDTO.getId() != null) {
            throw new BadRequestAlertException("A new diagnosis cannot already have an ID", ENTITY_NAME, "idexists");
        }
        diagnosisDTO = diagnosisService.save(diagnosisDTO);
        return ResponseEntity.created(new URI("/api/diagnoses/" + diagnosisDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, diagnosisDTO.getId().toString()))
            .body(diagnosisDTO);
    }

    /**
     * {@code PUT  /diagnoses/:id} : Updates an existing diagnosis.
     *
     * @param id the id of the diagnosisDTO to save.
     * @param diagnosisDTO the diagnosisDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated diagnosisDTO,
     * or with status {@code 400 (Bad Request)} if the diagnosisDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the diagnosisDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<DiagnosisDTO> updateDiagnosis(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody DiagnosisDTO diagnosisDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Diagnosis : {}, {}", id, diagnosisDTO);
        if (diagnosisDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, diagnosisDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!diagnosisRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        diagnosisDTO = diagnosisService.update(diagnosisDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, diagnosisDTO.getId().toString()))
            .body(diagnosisDTO);
    }

    /**
     * {@code PATCH  /diagnoses/:id} : Partial updates given fields of an existing diagnosis, field will ignore if it is null
     *
     * @param id the id of the diagnosisDTO to save.
     * @param diagnosisDTO the diagnosisDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated diagnosisDTO,
     * or with status {@code 400 (Bad Request)} if the diagnosisDTO is not valid,
     * or with status {@code 404 (Not Found)} if the diagnosisDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the diagnosisDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<DiagnosisDTO> partialUpdateDiagnosis(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody DiagnosisDTO diagnosisDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Diagnosis partially : {}, {}", id, diagnosisDTO);
        if (diagnosisDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, diagnosisDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!diagnosisRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<DiagnosisDTO> result = diagnosisService.partialUpdate(diagnosisDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, diagnosisDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /diagnoses} : get all the Diagnoses.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Diagnoses in body.
     */
    @GetMapping("")
    public List<DiagnosisDTO> getAllDiagnoses() {
        LOG.debug("REST request to get all Diagnoses");
        return diagnosisService.findAll();
    }

    /**
     * {@code GET  /diagnoses/:id} : get the "id" diagnosis.
     *
     * @param id the id of the diagnosisDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the diagnosisDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<DiagnosisDTO> getDiagnosis(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Diagnosis : {}", id);
        Optional<DiagnosisDTO> diagnosisDTO = diagnosisService.findOne(id);
        return ResponseUtil.wrapOrNotFound(diagnosisDTO);
    }

    /**
     * {@code DELETE  /diagnoses/:id} : delete the "id" diagnosis.
     *
     * @param id the id of the diagnosisDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDiagnosis(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Diagnosis : {}", id);
        diagnosisService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
