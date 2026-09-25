package com.hyperbrains.hms.web.rest;

import com.hyperbrains.hms.repository.WardRepository;
import com.hyperbrains.hms.service.WardService;
import com.hyperbrains.hms.service.dto.WardDTO;
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
 * REST controller for managing {@link com.hyperbrains.hms.domain.Ward}.
 */
@RestController
@RequestMapping("/api/wards")
public class WardResource {

    private static final Logger LOG = LoggerFactory.getLogger(WardResource.class);

    private static final String ENTITY_NAME = "ward";

    @Value("${jhipster.clientApp.name:hms}")
    private String applicationName;

    private final WardService wardService;

    private final WardRepository wardRepository;

    public WardResource(WardService wardService, WardRepository wardRepository) {
        this.wardService = wardService;
        this.wardRepository = wardRepository;
    }

    /**
     * {@code POST  /wards} : Create a new ward.
     *
     * @param wardDTO the wardDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new wardDTO, or with status {@code 400 (Bad Request)} if the ward has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<WardDTO> createWard(@Valid @RequestBody WardDTO wardDTO) throws URISyntaxException {
        LOG.debug("REST request to save Ward : {}", wardDTO);
        if (wardDTO.getId() != null) {
            throw new BadRequestAlertException("A new ward cannot already have an ID", ENTITY_NAME, "idexists");
        }
        wardDTO = wardService.save(wardDTO);
        return ResponseEntity.created(new URI("/api/wards/" + wardDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, wardDTO.getId().toString()))
            .body(wardDTO);
    }

    /**
     * {@code PUT  /wards/:id} : Updates an existing ward.
     *
     * @param id the id of the wardDTO to save.
     * @param wardDTO the wardDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated wardDTO,
     * or with status {@code 400 (Bad Request)} if the wardDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the wardDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<WardDTO> updateWard(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody WardDTO wardDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Ward : {}, {}", id, wardDTO);
        if (wardDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, wardDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!wardRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        wardDTO = wardService.update(wardDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, wardDTO.getId().toString()))
            .body(wardDTO);
    }

    /**
     * {@code PATCH  /wards/:id} : Partial updates given fields of an existing ward, field will ignore if it is null
     *
     * @param id the id of the wardDTO to save.
     * @param wardDTO the wardDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated wardDTO,
     * or with status {@code 400 (Bad Request)} if the wardDTO is not valid,
     * or with status {@code 404 (Not Found)} if the wardDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the wardDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<WardDTO> partialUpdateWard(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody WardDTO wardDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Ward partially : {}, {}", id, wardDTO);
        if (wardDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, wardDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!wardRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<WardDTO> result = wardService.partialUpdate(wardDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, wardDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /wards} : get all the Wards.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Wards in body.
     */
    @GetMapping("")
    public List<WardDTO> getAllWards() {
        LOG.debug("REST request to get all Wards");
        return wardService.findAll();
    }

    /**
     * {@code GET  /wards/:id} : get the "id" ward.
     *
     * @param id the id of the wardDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the wardDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<WardDTO> getWard(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Ward : {}", id);
        Optional<WardDTO> wardDTO = wardService.findOne(id);
        return ResponseUtil.wrapOrNotFound(wardDTO);
    }

    /**
     * {@code DELETE  /wards/:id} : delete the "id" ward.
     *
     * @param id the id of the wardDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWard(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Ward : {}", id);
        wardService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
