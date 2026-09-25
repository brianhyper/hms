package com.hyperbrains.hms.web.rest;

import com.hyperbrains.hms.repository.DispenseRepository;
import com.hyperbrains.hms.service.DispenseService;
import com.hyperbrains.hms.service.dto.DispenseDTO;
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
 * REST controller for managing {@link com.hyperbrains.hms.domain.Dispense}.
 */
@RestController
@RequestMapping("/api/dispenses")
public class DispenseResource {

    private static final Logger LOG = LoggerFactory.getLogger(DispenseResource.class);

    private static final String ENTITY_NAME = "dispense";

    @Value("${jhipster.clientApp.name:hms}")
    private String applicationName;

    private final DispenseService dispenseService;

    private final DispenseRepository dispenseRepository;

    public DispenseResource(DispenseService dispenseService, DispenseRepository dispenseRepository) {
        this.dispenseService = dispenseService;
        this.dispenseRepository = dispenseRepository;
    }

    /**
     * {@code POST  /dispenses} : Create a new dispense.
     *
     * @param dispenseDTO the dispenseDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new dispenseDTO, or with status {@code 400 (Bad Request)} if the dispense has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<DispenseDTO> createDispense(@Valid @RequestBody DispenseDTO dispenseDTO) throws URISyntaxException {
        LOG.debug("REST request to save Dispense : {}", dispenseDTO);
        if (dispenseDTO.getId() != null) {
            throw new BadRequestAlertException("A new dispense cannot already have an ID", ENTITY_NAME, "idexists");
        }
        dispenseDTO = dispenseService.save(dispenseDTO);
        return ResponseEntity.created(new URI("/api/dispenses/" + dispenseDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, dispenseDTO.getId().toString()))
            .body(dispenseDTO);
    }

    /**
     * {@code PUT  /dispenses/:id} : Updates an existing dispense.
     *
     * @param id the id of the dispenseDTO to save.
     * @param dispenseDTO the dispenseDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated dispenseDTO,
     * or with status {@code 400 (Bad Request)} if the dispenseDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the dispenseDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<DispenseDTO> updateDispense(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody DispenseDTO dispenseDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Dispense : {}, {}", id, dispenseDTO);
        if (dispenseDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, dispenseDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!dispenseRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        dispenseDTO = dispenseService.update(dispenseDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, dispenseDTO.getId().toString()))
            .body(dispenseDTO);
    }

    /**
     * {@code PATCH  /dispenses/:id} : Partial updates given fields of an existing dispense, field will ignore if it is null
     *
     * @param id the id of the dispenseDTO to save.
     * @param dispenseDTO the dispenseDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated dispenseDTO,
     * or with status {@code 400 (Bad Request)} if the dispenseDTO is not valid,
     * or with status {@code 404 (Not Found)} if the dispenseDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the dispenseDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<DispenseDTO> partialUpdateDispense(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody DispenseDTO dispenseDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Dispense partially : {}, {}", id, dispenseDTO);
        if (dispenseDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, dispenseDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!dispenseRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<DispenseDTO> result = dispenseService.partialUpdate(dispenseDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, dispenseDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /dispenses} : get all the Dispenses.
     *
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Dispenses in body.
     */
    @GetMapping("")
    public List<DispenseDTO> getAllDispenses(@RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload) {
        LOG.debug("REST request to get all Dispenses");
        return dispenseService.findAll();
    }

    /**
     * {@code GET  /dispenses/:id} : get the "id" dispense.
     *
     * @param id the id of the dispenseDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the dispenseDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<DispenseDTO> getDispense(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Dispense : {}", id);
        Optional<DispenseDTO> dispenseDTO = dispenseService.findOne(id);
        return ResponseUtil.wrapOrNotFound(dispenseDTO);
    }

    /**
     * {@code DELETE  /dispenses/:id} : delete the "id" dispense.
     *
     * @param id the id of the dispenseDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDispense(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Dispense : {}", id);
        dispenseService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
