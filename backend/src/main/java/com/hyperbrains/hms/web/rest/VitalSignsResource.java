package com.hyperbrains.hms.web.rest;

import com.hyperbrains.hms.repository.VitalSignsRepository;
import com.hyperbrains.hms.service.VitalSignsService;
import com.hyperbrains.hms.service.dto.VitalSignsDTO;
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
 * REST controller for managing {@link com.hyperbrains.hms.domain.VitalSigns}.
 */
@RestController
@RequestMapping("/api/vital-signs")
public class VitalSignsResource {

    private static final Logger LOG = LoggerFactory.getLogger(VitalSignsResource.class);

    private static final String ENTITY_NAME = "vitalSigns";

    @Value("${jhipster.clientApp.name:hms}")
    private String applicationName;

    private final VitalSignsService vitalSignsService;

    private final VitalSignsRepository vitalSignsRepository;

    public VitalSignsResource(VitalSignsService vitalSignsService, VitalSignsRepository vitalSignsRepository) {
        this.vitalSignsService = vitalSignsService;
        this.vitalSignsRepository = vitalSignsRepository;
    }

    /**
     * {@code POST  /vital-signs} : Create a new vitalSigns.
     *
     * @param vitalSignsDTO the vitalSignsDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new vitalSignsDTO, or with status {@code 400 (Bad Request)} if the vitalSigns has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<VitalSignsDTO> createVitalSigns(@Valid @RequestBody VitalSignsDTO vitalSignsDTO) throws URISyntaxException {
        LOG.debug("REST request to save VitalSigns : {}", vitalSignsDTO);
        if (vitalSignsDTO.getId() != null) {
            throw new BadRequestAlertException("A new vitalSigns cannot already have an ID", ENTITY_NAME, "idexists");
        }
        vitalSignsDTO = vitalSignsService.save(vitalSignsDTO);
        return ResponseEntity.created(new URI("/api/vital-signs/" + vitalSignsDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, vitalSignsDTO.getId().toString()))
            .body(vitalSignsDTO);
    }

    /**
     * {@code PUT  /vital-signs/:id} : Updates an existing vitalSigns.
     *
     * @param id the id of the vitalSignsDTO to save.
     * @param vitalSignsDTO the vitalSignsDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated vitalSignsDTO,
     * or with status {@code 400 (Bad Request)} if the vitalSignsDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the vitalSignsDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<VitalSignsDTO> updateVitalSigns(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody VitalSignsDTO vitalSignsDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update VitalSigns : {}, {}", id, vitalSignsDTO);
        if (vitalSignsDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, vitalSignsDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!vitalSignsRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        vitalSignsDTO = vitalSignsService.update(vitalSignsDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, vitalSignsDTO.getId().toString()))
            .body(vitalSignsDTO);
    }

    /**
     * {@code PATCH  /vital-signs/:id} : Partial updates given fields of an existing vitalSigns, field will ignore if it is null
     *
     * @param id the id of the vitalSignsDTO to save.
     * @param vitalSignsDTO the vitalSignsDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated vitalSignsDTO,
     * or with status {@code 400 (Bad Request)} if the vitalSignsDTO is not valid,
     * or with status {@code 404 (Not Found)} if the vitalSignsDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the vitalSignsDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<VitalSignsDTO> partialUpdateVitalSigns(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody VitalSignsDTO vitalSignsDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update VitalSigns partially : {}, {}", id, vitalSignsDTO);
        if (vitalSignsDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, vitalSignsDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!vitalSignsRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<VitalSignsDTO> result = vitalSignsService.partialUpdate(vitalSignsDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, vitalSignsDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /vital-signs} : get all the Vital Signs.
     *
     * @param filter the filter of the request.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Vital Signs in body.
     */
    @GetMapping("")
    public List<VitalSignsDTO> getAllVitalSignses(@RequestParam(name = "filter", required = false) String filter) {
        if ("visit-is-null".equals(filter)) {
            LOG.debug("REST request to get all VitalSignss where visit is null");
            return vitalSignsService.findAllWhereVisitIsNull();
        }
        LOG.debug("REST request to get all VitalSignses");
        return vitalSignsService.findAll();
    }

    /**
     * {@code GET  /vital-signs/:id} : get the "id" vitalSigns.
     *
     * @param id the id of the vitalSignsDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the vitalSignsDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<VitalSignsDTO> getVitalSigns(@PathVariable("id") Long id) {
        LOG.debug("REST request to get VitalSigns : {}", id);
        Optional<VitalSignsDTO> vitalSignsDTO = vitalSignsService.findOne(id);
        return ResponseUtil.wrapOrNotFound(vitalSignsDTO);
    }

    /**
     * {@code DELETE  /vital-signs/:id} : delete the "id" vitalSigns.
     *
     * @param id the id of the vitalSignsDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVitalSigns(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete VitalSigns : {}", id);
        vitalSignsService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
