package com.hyperbrains.hms.web.rest;

import com.hyperbrains.hms.repository.AdmissionRepository;
import com.hyperbrains.hms.service.AdmissionService;
import com.hyperbrains.hms.service.dto.AdmissionDTO;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.hyperbrains.hms.domain.Admission}.
 */
@RestController
@RequestMapping("/api/admissions")
public class AdmissionResource {

    private static final Logger LOG = LoggerFactory.getLogger(AdmissionResource.class);

    private static final String ENTITY_NAME = "admission";

    @Value("${jhipster.clientApp.name:hms}")
    private String applicationName;

    private final AdmissionService admissionService;

    private final AdmissionRepository admissionRepository;

    public AdmissionResource(AdmissionService admissionService, AdmissionRepository admissionRepository) {
        this.admissionService = admissionService;
        this.admissionRepository = admissionRepository;
    }

    /**
     * {@code POST  /admissions} : Create a new admission.
     *
     * @param admissionDTO the admissionDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new admissionDTO, or with status {@code 400 (Bad Request)} if the admission has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<AdmissionDTO> createAdmission(@Valid @RequestBody AdmissionDTO admissionDTO) throws URISyntaxException {
        LOG.debug("REST request to save Admission : {}", admissionDTO);
        if (admissionDTO.getId() != null) {
            throw new BadRequestAlertException("A new admission cannot already have an ID", ENTITY_NAME, "idexists");
        }
        admissionDTO = admissionService.save(admissionDTO);
        return ResponseEntity.created(new URI("/api/admissions/" + admissionDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, admissionDTO.getId().toString()))
            .body(admissionDTO);
    }

    /**
     * {@code PUT  /admissions/:id} : Updates an existing admission.
     *
     * @param id the id of the admissionDTO to save.
     * @param admissionDTO the admissionDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated admissionDTO,
     * or with status {@code 400 (Bad Request)} if the admissionDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the admissionDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<AdmissionDTO> updateAdmission(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody AdmissionDTO admissionDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Admission : {}, {}", id, admissionDTO);
        if (admissionDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, admissionDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!admissionRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        admissionDTO = admissionService.update(admissionDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, admissionDTO.getId().toString()))
            .body(admissionDTO);
    }

    /**
     * {@code PATCH  /admissions/:id} : Partial updates given fields of an existing admission, field will ignore if it is null
     *
     * @param id the id of the admissionDTO to save.
     * @param admissionDTO the admissionDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated admissionDTO,
     * or with status {@code 400 (Bad Request)} if the admissionDTO is not valid,
     * or with status {@code 404 (Not Found)} if the admissionDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the admissionDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<AdmissionDTO> partialUpdateAdmission(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody AdmissionDTO admissionDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Admission partially : {}, {}", id, admissionDTO);
        if (admissionDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, admissionDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!admissionRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<AdmissionDTO> result = admissionService.partialUpdate(admissionDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, admissionDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /admissions} : get all the Admissions.
     *
     * @param pageable the pagination information.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Admissions in body.
     */
    @GetMapping("")
    public ResponseEntity<List<AdmissionDTO>> getAllAdmissions(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to get a page of Admissions");
        Page<AdmissionDTO> page;
        if (eagerload) {
            page = admissionService.findAllWithEagerRelationships(pageable);
        } else {
            page = admissionService.findAll(pageable);
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /admissions/:id} : get the "id" admission.
     *
     * @param id the id of the admissionDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the admissionDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AdmissionDTO> getAdmission(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Admission : {}", id);
        Optional<AdmissionDTO> admissionDTO = admissionService.findOne(id);
        return ResponseUtil.wrapOrNotFound(admissionDTO);
    }

    /**
     * {@code DELETE  /admissions/:id} : delete the "id" admission.
     *
     * @param id the id of the admissionDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAdmission(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Admission : {}", id);
        admissionService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
