package com.hyperbrains.hms.web.rest;

import com.hyperbrains.hms.repository.DispenseLineRepository;
import com.hyperbrains.hms.service.DispenseLineService;
import com.hyperbrains.hms.service.dto.DispenseLineDTO;
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
 * REST controller for managing {@link com.hyperbrains.hms.domain.DispenseLine}.
 */
@RestController
@RequestMapping("/api/dispense-lines")
public class DispenseLineResource {

    private static final Logger LOG = LoggerFactory.getLogger(DispenseLineResource.class);

    private static final String ENTITY_NAME = "dispenseLine";

    @Value("${jhipster.clientApp.name:hms}")
    private String applicationName;

    private final DispenseLineService dispenseLineService;

    private final DispenseLineRepository dispenseLineRepository;

    public DispenseLineResource(DispenseLineService dispenseLineService, DispenseLineRepository dispenseLineRepository) {
        this.dispenseLineService = dispenseLineService;
        this.dispenseLineRepository = dispenseLineRepository;
    }

    /**
     * {@code POST  /dispense-lines} : Create a new dispenseLine.
     *
     * @param dispenseLineDTO the dispenseLineDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new dispenseLineDTO, or with status {@code 400 (Bad Request)} if the dispenseLine has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<DispenseLineDTO> createDispenseLine(@Valid @RequestBody DispenseLineDTO dispenseLineDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save DispenseLine : {}", dispenseLineDTO);
        if (dispenseLineDTO.getId() != null) {
            throw new BadRequestAlertException("A new dispenseLine cannot already have an ID", ENTITY_NAME, "idexists");
        }
        dispenseLineDTO = dispenseLineService.save(dispenseLineDTO);
        return ResponseEntity.created(new URI("/api/dispense-lines/" + dispenseLineDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, dispenseLineDTO.getId().toString()))
            .body(dispenseLineDTO);
    }

    /**
     * {@code PUT  /dispense-lines/:id} : Updates an existing dispenseLine.
     *
     * @param id the id of the dispenseLineDTO to save.
     * @param dispenseLineDTO the dispenseLineDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated dispenseLineDTO,
     * or with status {@code 400 (Bad Request)} if the dispenseLineDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the dispenseLineDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<DispenseLineDTO> updateDispenseLine(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody DispenseLineDTO dispenseLineDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update DispenseLine : {}, {}", id, dispenseLineDTO);
        if (dispenseLineDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, dispenseLineDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!dispenseLineRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        dispenseLineDTO = dispenseLineService.update(dispenseLineDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, dispenseLineDTO.getId().toString()))
            .body(dispenseLineDTO);
    }

    /**
     * {@code PATCH  /dispense-lines/:id} : Partial updates given fields of an existing dispenseLine, field will ignore if it is null
     *
     * @param id the id of the dispenseLineDTO to save.
     * @param dispenseLineDTO the dispenseLineDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated dispenseLineDTO,
     * or with status {@code 400 (Bad Request)} if the dispenseLineDTO is not valid,
     * or with status {@code 404 (Not Found)} if the dispenseLineDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the dispenseLineDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<DispenseLineDTO> partialUpdateDispenseLine(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody DispenseLineDTO dispenseLineDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update DispenseLine partially : {}, {}", id, dispenseLineDTO);
        if (dispenseLineDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, dispenseLineDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!dispenseLineRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<DispenseLineDTO> result = dispenseLineService.partialUpdate(dispenseLineDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, dispenseLineDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /dispense-lines} : get all the Dispense Lines.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Dispense Lines in body.
     */
    @GetMapping("")
    public List<DispenseLineDTO> getAllDispenseLines() {
        LOG.debug("REST request to get all DispenseLines");
        return dispenseLineService.findAll();
    }

    /**
     * {@code GET  /dispense-lines/:id} : get the "id" dispenseLine.
     *
     * @param id the id of the dispenseLineDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the dispenseLineDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<DispenseLineDTO> getDispenseLine(@PathVariable("id") Long id) {
        LOG.debug("REST request to get DispenseLine : {}", id);
        Optional<DispenseLineDTO> dispenseLineDTO = dispenseLineService.findOne(id);
        return ResponseUtil.wrapOrNotFound(dispenseLineDTO);
    }

    /**
     * {@code DELETE  /dispense-lines/:id} : delete the "id" dispenseLine.
     *
     * @param id the id of the dispenseLineDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDispenseLine(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete DispenseLine : {}", id);
        dispenseLineService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
