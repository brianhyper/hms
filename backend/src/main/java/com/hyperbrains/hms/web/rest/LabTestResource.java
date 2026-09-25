package com.hyperbrains.hms.web.rest;

import com.hyperbrains.hms.repository.LabTestRepository;
import com.hyperbrains.hms.service.LabTestService;
import com.hyperbrains.hms.service.dto.LabTestDTO;
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
 * REST controller for managing {@link com.hyperbrains.hms.domain.LabTest}.
 */
@RestController
@RequestMapping("/api/lab-tests")
public class LabTestResource {

    private static final Logger LOG = LoggerFactory.getLogger(LabTestResource.class);

    private static final String ENTITY_NAME = "labTest";

    @Value("${jhipster.clientApp.name:hms}")
    private String applicationName;

    private final LabTestService labTestService;

    private final LabTestRepository labTestRepository;

    public LabTestResource(LabTestService labTestService, LabTestRepository labTestRepository) {
        this.labTestService = labTestService;
        this.labTestRepository = labTestRepository;
    }

    /**
     * {@code POST  /lab-tests} : Create a new labTest.
     *
     * @param labTestDTO the labTestDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new labTestDTO, or with status {@code 400 (Bad Request)} if the labTest has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<LabTestDTO> createLabTest(@Valid @RequestBody LabTestDTO labTestDTO) throws URISyntaxException {
        LOG.debug("REST request to save LabTest : {}", labTestDTO);
        if (labTestDTO.getId() != null) {
            throw new BadRequestAlertException("A new labTest cannot already have an ID", ENTITY_NAME, "idexists");
        }
        labTestDTO = labTestService.save(labTestDTO);
        return ResponseEntity.created(new URI("/api/lab-tests/" + labTestDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, labTestDTO.getId().toString()))
            .body(labTestDTO);
    }

    /**
     * {@code PUT  /lab-tests/:id} : Updates an existing labTest.
     *
     * @param id the id of the labTestDTO to save.
     * @param labTestDTO the labTestDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated labTestDTO,
     * or with status {@code 400 (Bad Request)} if the labTestDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the labTestDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<LabTestDTO> updateLabTest(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody LabTestDTO labTestDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update LabTest : {}, {}", id, labTestDTO);
        if (labTestDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, labTestDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!labTestRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        labTestDTO = labTestService.update(labTestDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, labTestDTO.getId().toString()))
            .body(labTestDTO);
    }

    /**
     * {@code PATCH  /lab-tests/:id} : Partial updates given fields of an existing labTest, field will ignore if it is null
     *
     * @param id the id of the labTestDTO to save.
     * @param labTestDTO the labTestDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated labTestDTO,
     * or with status {@code 400 (Bad Request)} if the labTestDTO is not valid,
     * or with status {@code 404 (Not Found)} if the labTestDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the labTestDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<LabTestDTO> partialUpdateLabTest(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody LabTestDTO labTestDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update LabTest partially : {}, {}", id, labTestDTO);
        if (labTestDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, labTestDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!labTestRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<LabTestDTO> result = labTestService.partialUpdate(labTestDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, labTestDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /lab-tests} : get all the Lab Tests.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Lab Tests in body.
     */
    @GetMapping("")
    public List<LabTestDTO> getAllLabTests() {
        LOG.debug("REST request to get all LabTests");
        return labTestService.findAll();
    }

    /**
     * {@code GET  /lab-tests/:id} : get the "id" labTest.
     *
     * @param id the id of the labTestDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the labTestDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<LabTestDTO> getLabTest(@PathVariable("id") Long id) {
        LOG.debug("REST request to get LabTest : {}", id);
        Optional<LabTestDTO> labTestDTO = labTestService.findOne(id);
        return ResponseUtil.wrapOrNotFound(labTestDTO);
    }

    /**
     * {@code DELETE  /lab-tests/:id} : delete the "id" labTest.
     *
     * @param id the id of the labTestDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLabTest(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete LabTest : {}", id);
        labTestService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
