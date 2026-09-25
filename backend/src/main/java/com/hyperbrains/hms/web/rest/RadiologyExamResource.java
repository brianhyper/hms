package com.hyperbrains.hms.web.rest;

import com.hyperbrains.hms.repository.RadiologyExamRepository;
import com.hyperbrains.hms.service.RadiologyExamService;
import com.hyperbrains.hms.service.dto.RadiologyExamDTO;
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
 * REST controller for managing {@link com.hyperbrains.hms.domain.RadiologyExam}.
 */
@RestController
@RequestMapping("/api/radiology-exams")
public class RadiologyExamResource {

    private static final Logger LOG = LoggerFactory.getLogger(RadiologyExamResource.class);

    private static final String ENTITY_NAME = "radiologyExam";

    @Value("${jhipster.clientApp.name:hms}")
    private String applicationName;

    private final RadiologyExamService radiologyExamService;

    private final RadiologyExamRepository radiologyExamRepository;

    public RadiologyExamResource(RadiologyExamService radiologyExamService, RadiologyExamRepository radiologyExamRepository) {
        this.radiologyExamService = radiologyExamService;
        this.radiologyExamRepository = radiologyExamRepository;
    }

    /**
     * {@code POST  /radiology-exams} : Create a new radiologyExam.
     *
     * @param radiologyExamDTO the radiologyExamDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new radiologyExamDTO, or with status {@code 400 (Bad Request)} if the radiologyExam has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<RadiologyExamDTO> createRadiologyExam(@Valid @RequestBody RadiologyExamDTO radiologyExamDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save RadiologyExam : {}", radiologyExamDTO);
        if (radiologyExamDTO.getId() != null) {
            throw new BadRequestAlertException("A new radiologyExam cannot already have an ID", ENTITY_NAME, "idexists");
        }
        radiologyExamDTO = radiologyExamService.save(radiologyExamDTO);
        return ResponseEntity.created(new URI("/api/radiology-exams/" + radiologyExamDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, radiologyExamDTO.getId().toString()))
            .body(radiologyExamDTO);
    }

    /**
     * {@code PUT  /radiology-exams/:id} : Updates an existing radiologyExam.
     *
     * @param id the id of the radiologyExamDTO to save.
     * @param radiologyExamDTO the radiologyExamDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated radiologyExamDTO,
     * or with status {@code 400 (Bad Request)} if the radiologyExamDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the radiologyExamDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<RadiologyExamDTO> updateRadiologyExam(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody RadiologyExamDTO radiologyExamDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update RadiologyExam : {}, {}", id, radiologyExamDTO);
        if (radiologyExamDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, radiologyExamDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!radiologyExamRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        radiologyExamDTO = radiologyExamService.update(radiologyExamDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, radiologyExamDTO.getId().toString()))
            .body(radiologyExamDTO);
    }

    /**
     * {@code PATCH  /radiology-exams/:id} : Partial updates given fields of an existing radiologyExam, field will ignore if it is null
     *
     * @param id the id of the radiologyExamDTO to save.
     * @param radiologyExamDTO the radiologyExamDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated radiologyExamDTO,
     * or with status {@code 400 (Bad Request)} if the radiologyExamDTO is not valid,
     * or with status {@code 404 (Not Found)} if the radiologyExamDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the radiologyExamDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<RadiologyExamDTO> partialUpdateRadiologyExam(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody RadiologyExamDTO radiologyExamDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update RadiologyExam partially : {}, {}", id, radiologyExamDTO);
        if (radiologyExamDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, radiologyExamDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!radiologyExamRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<RadiologyExamDTO> result = radiologyExamService.partialUpdate(radiologyExamDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, radiologyExamDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /radiology-exams} : get all the Radiology Exams.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Radiology Exams in body.
     */
    @GetMapping("")
    public List<RadiologyExamDTO> getAllRadiologyExams() {
        LOG.debug("REST request to get all RadiologyExams");
        return radiologyExamService.findAll();
    }

    /**
     * {@code GET  /radiology-exams/:id} : get the "id" radiologyExam.
     *
     * @param id the id of the radiologyExamDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the radiologyExamDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<RadiologyExamDTO> getRadiologyExam(@PathVariable("id") Long id) {
        LOG.debug("REST request to get RadiologyExam : {}", id);
        Optional<RadiologyExamDTO> radiologyExamDTO = radiologyExamService.findOne(id);
        return ResponseUtil.wrapOrNotFound(radiologyExamDTO);
    }

    /**
     * {@code DELETE  /radiology-exams/:id} : delete the "id" radiologyExam.
     *
     * @param id the id of the radiologyExamDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRadiologyExam(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete RadiologyExam : {}", id);
        radiologyExamService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
