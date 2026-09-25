package com.hyperbrains.hms.web.rest;

import com.hyperbrains.hms.repository.BedTypeRepository;
import com.hyperbrains.hms.service.BedTypeService;
import com.hyperbrains.hms.service.dto.BedTypeDTO;
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
 * REST controller for managing {@link com.hyperbrains.hms.domain.BedType}.
 */
@RestController
@RequestMapping("/api/bed-types")
public class BedTypeResource {

    private static final Logger LOG = LoggerFactory.getLogger(BedTypeResource.class);

    private static final String ENTITY_NAME = "bedType";

    @Value("${jhipster.clientApp.name:hms}")
    private String applicationName;

    private final BedTypeService bedTypeService;

    private final BedTypeRepository bedTypeRepository;

    public BedTypeResource(BedTypeService bedTypeService, BedTypeRepository bedTypeRepository) {
        this.bedTypeService = bedTypeService;
        this.bedTypeRepository = bedTypeRepository;
    }

    /**
     * {@code POST  /bed-types} : Create a new bedType.
     *
     * @param bedTypeDTO the bedTypeDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new bedTypeDTO, or with status {@code 400 (Bad Request)} if the bedType has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<BedTypeDTO> createBedType(@Valid @RequestBody BedTypeDTO bedTypeDTO) throws URISyntaxException {
        LOG.debug("REST request to save BedType : {}", bedTypeDTO);
        if (bedTypeDTO.getId() != null) {
            throw new BadRequestAlertException("A new bedType cannot already have an ID", ENTITY_NAME, "idexists");
        }
        bedTypeDTO = bedTypeService.save(bedTypeDTO);
        return ResponseEntity.created(new URI("/api/bed-types/" + bedTypeDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, bedTypeDTO.getId().toString()))
            .body(bedTypeDTO);
    }

    /**
     * {@code PUT  /bed-types/:id} : Updates an existing bedType.
     *
     * @param id the id of the bedTypeDTO to save.
     * @param bedTypeDTO the bedTypeDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated bedTypeDTO,
     * or with status {@code 400 (Bad Request)} if the bedTypeDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the bedTypeDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<BedTypeDTO> updateBedType(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody BedTypeDTO bedTypeDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update BedType : {}, {}", id, bedTypeDTO);
        if (bedTypeDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, bedTypeDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!bedTypeRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        bedTypeDTO = bedTypeService.update(bedTypeDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, bedTypeDTO.getId().toString()))
            .body(bedTypeDTO);
    }

    /**
     * {@code PATCH  /bed-types/:id} : Partial updates given fields of an existing bedType, field will ignore if it is null
     *
     * @param id the id of the bedTypeDTO to save.
     * @param bedTypeDTO the bedTypeDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated bedTypeDTO,
     * or with status {@code 400 (Bad Request)} if the bedTypeDTO is not valid,
     * or with status {@code 404 (Not Found)} if the bedTypeDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the bedTypeDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<BedTypeDTO> partialUpdateBedType(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody BedTypeDTO bedTypeDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update BedType partially : {}, {}", id, bedTypeDTO);
        if (bedTypeDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, bedTypeDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!bedTypeRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<BedTypeDTO> result = bedTypeService.partialUpdate(bedTypeDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, bedTypeDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /bed-types} : get all the Bed Types.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Bed Types in body.
     */
    @GetMapping("")
    public List<BedTypeDTO> getAllBedTypes() {
        LOG.debug("REST request to get all BedTypes");
        return bedTypeService.findAll();
    }

    /**
     * {@code GET  /bed-types/:id} : get the "id" bedType.
     *
     * @param id the id of the bedTypeDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the bedTypeDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<BedTypeDTO> getBedType(@PathVariable("id") Long id) {
        LOG.debug("REST request to get BedType : {}", id);
        Optional<BedTypeDTO> bedTypeDTO = bedTypeService.findOne(id);
        return ResponseUtil.wrapOrNotFound(bedTypeDTO);
    }

    /**
     * {@code DELETE  /bed-types/:id} : delete the "id" bedType.
     *
     * @param id the id of the bedTypeDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBedType(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete BedType : {}", id);
        bedTypeService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
