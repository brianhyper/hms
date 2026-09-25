package com.hyperbrains.hms.web.rest;

import com.hyperbrains.hms.repository.WardCoverRepository;
import com.hyperbrains.hms.service.WardCoverService;
import com.hyperbrains.hms.service.dto.WardCoverDTO;
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
 * REST controller for managing {@link com.hyperbrains.hms.domain.WardCover}.
 */
@RestController
@RequestMapping("/api/ward-covers")
public class WardCoverResource {

    private static final Logger LOG = LoggerFactory.getLogger(WardCoverResource.class);

    private static final String ENTITY_NAME = "wardCover";

    @Value("${jhipster.clientApp.name:hms}")
    private String applicationName;

    private final WardCoverService wardCoverService;

    private final WardCoverRepository wardCoverRepository;

    public WardCoverResource(WardCoverService wardCoverService, WardCoverRepository wardCoverRepository) {
        this.wardCoverService = wardCoverService;
        this.wardCoverRepository = wardCoverRepository;
    }

    /**
     * {@code POST  /ward-covers} : Create a new wardCover.
     *
     * @param wardCoverDTO the wardCoverDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new wardCoverDTO, or with status {@code 400 (Bad Request)} if the wardCover has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<WardCoverDTO> createWardCover(@Valid @RequestBody WardCoverDTO wardCoverDTO) throws URISyntaxException {
        LOG.debug("REST request to save WardCover : {}", wardCoverDTO);
        if (wardCoverDTO.getId() != null) {
            throw new BadRequestAlertException("A new wardCover cannot already have an ID", ENTITY_NAME, "idexists");
        }
        wardCoverDTO = wardCoverService.save(wardCoverDTO);
        return ResponseEntity.created(new URI("/api/ward-covers/" + wardCoverDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, wardCoverDTO.getId().toString()))
            .body(wardCoverDTO);
    }

    /**
     * {@code PUT  /ward-covers/:id} : Updates an existing wardCover.
     *
     * @param id the id of the wardCoverDTO to save.
     * @param wardCoverDTO the wardCoverDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated wardCoverDTO,
     * or with status {@code 400 (Bad Request)} if the wardCoverDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the wardCoverDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<WardCoverDTO> updateWardCover(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody WardCoverDTO wardCoverDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update WardCover : {}, {}", id, wardCoverDTO);
        if (wardCoverDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, wardCoverDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!wardCoverRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        wardCoverDTO = wardCoverService.update(wardCoverDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, wardCoverDTO.getId().toString()))
            .body(wardCoverDTO);
    }

    /**
     * {@code PATCH  /ward-covers/:id} : Partial updates given fields of an existing wardCover, field will ignore if it is null
     *
     * @param id the id of the wardCoverDTO to save.
     * @param wardCoverDTO the wardCoverDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated wardCoverDTO,
     * or with status {@code 400 (Bad Request)} if the wardCoverDTO is not valid,
     * or with status {@code 404 (Not Found)} if the wardCoverDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the wardCoverDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<WardCoverDTO> partialUpdateWardCover(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody WardCoverDTO wardCoverDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update WardCover partially : {}, {}", id, wardCoverDTO);
        if (wardCoverDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, wardCoverDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!wardCoverRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<WardCoverDTO> result = wardCoverService.partialUpdate(wardCoverDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, wardCoverDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /ward-covers} : get all the Ward Covers.
     *
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Ward Covers in body.
     */
    @GetMapping("")
    public List<WardCoverDTO> getAllWardCovers(
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to get all WardCovers");
        return wardCoverService.findAll();
    }

    /**
     * {@code GET  /ward-covers/:id} : get the "id" wardCover.
     *
     * @param id the id of the wardCoverDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the wardCoverDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<WardCoverDTO> getWardCover(@PathVariable("id") Long id) {
        LOG.debug("REST request to get WardCover : {}", id);
        Optional<WardCoverDTO> wardCoverDTO = wardCoverService.findOne(id);
        return ResponseUtil.wrapOrNotFound(wardCoverDTO);
    }

    /**
     * {@code DELETE  /ward-covers/:id} : delete the "id" wardCover.
     *
     * @param id the id of the wardCoverDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWardCover(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete WardCover : {}", id);
        wardCoverService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
