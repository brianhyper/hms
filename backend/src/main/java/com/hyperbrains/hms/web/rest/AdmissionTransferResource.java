package com.hyperbrains.hms.web.rest;

import com.hyperbrains.hms.repository.AdmissionTransferRepository;
import com.hyperbrains.hms.service.AdmissionTransferService;
import com.hyperbrains.hms.service.dto.AdmissionTransferDTO;
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
 * REST controller for managing {@link com.hyperbrains.hms.domain.AdmissionTransfer}.
 */
@RestController
@RequestMapping("/api/admission-transfers")
public class AdmissionTransferResource {

    private static final Logger LOG = LoggerFactory.getLogger(AdmissionTransferResource.class);

    private static final String ENTITY_NAME = "admissionTransfer";

    @Value("${jhipster.clientApp.name:hms}")
    private String applicationName;

    private final AdmissionTransferService admissionTransferService;

    private final AdmissionTransferRepository admissionTransferRepository;

    public AdmissionTransferResource(
        AdmissionTransferService admissionTransferService,
        AdmissionTransferRepository admissionTransferRepository
    ) {
        this.admissionTransferService = admissionTransferService;
        this.admissionTransferRepository = admissionTransferRepository;
    }

    /**
     * {@code POST  /admission-transfers} : Create a new admissionTransfer.
     *
     * @param admissionTransferDTO the admissionTransferDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new admissionTransferDTO, or with status {@code 400 (Bad Request)} if the admissionTransfer has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<AdmissionTransferDTO> createAdmissionTransfer(@Valid @RequestBody AdmissionTransferDTO admissionTransferDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save AdmissionTransfer : {}", admissionTransferDTO);
        if (admissionTransferDTO.getId() != null) {
            throw new BadRequestAlertException("A new admissionTransfer cannot already have an ID", ENTITY_NAME, "idexists");
        }
        admissionTransferDTO = admissionTransferService.save(admissionTransferDTO);
        return ResponseEntity.created(new URI("/api/admission-transfers/" + admissionTransferDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, admissionTransferDTO.getId().toString()))
            .body(admissionTransferDTO);
    }

    /**
     * {@code PUT  /admission-transfers/:id} : Updates an existing admissionTransfer.
     *
     * @param id the id of the admissionTransferDTO to save.
     * @param admissionTransferDTO the admissionTransferDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated admissionTransferDTO,
     * or with status {@code 400 (Bad Request)} if the admissionTransferDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the admissionTransferDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<AdmissionTransferDTO> updateAdmissionTransfer(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody AdmissionTransferDTO admissionTransferDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update AdmissionTransfer : {}, {}", id, admissionTransferDTO);
        if (admissionTransferDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, admissionTransferDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!admissionTransferRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        admissionTransferDTO = admissionTransferService.update(admissionTransferDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, admissionTransferDTO.getId().toString()))
            .body(admissionTransferDTO);
    }

    /**
     * {@code PATCH  /admission-transfers/:id} : Partial updates given fields of an existing admissionTransfer, field will ignore if it is null
     *
     * @param id the id of the admissionTransferDTO to save.
     * @param admissionTransferDTO the admissionTransferDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated admissionTransferDTO,
     * or with status {@code 400 (Bad Request)} if the admissionTransferDTO is not valid,
     * or with status {@code 404 (Not Found)} if the admissionTransferDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the admissionTransferDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<AdmissionTransferDTO> partialUpdateAdmissionTransfer(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody AdmissionTransferDTO admissionTransferDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update AdmissionTransfer partially : {}, {}", id, admissionTransferDTO);
        if (admissionTransferDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, admissionTransferDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!admissionTransferRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<AdmissionTransferDTO> result = admissionTransferService.partialUpdate(admissionTransferDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, admissionTransferDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /admission-transfers} : get all the Admission Transfers.
     *
     * @param pageable the pagination information.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Admission Transfers in body.
     */
    @GetMapping("")
    public ResponseEntity<List<AdmissionTransferDTO>> getAllAdmissionTransfers(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to get a page of AdmissionTransfers");
        Page<AdmissionTransferDTO> page;
        if (eagerload) {
            page = admissionTransferService.findAllWithEagerRelationships(pageable);
        } else {
            page = admissionTransferService.findAll(pageable);
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /admission-transfers/:id} : get the "id" admissionTransfer.
     *
     * @param id the id of the admissionTransferDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the admissionTransferDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AdmissionTransferDTO> getAdmissionTransfer(@PathVariable("id") Long id) {
        LOG.debug("REST request to get AdmissionTransfer : {}", id);
        Optional<AdmissionTransferDTO> admissionTransferDTO = admissionTransferService.findOne(id);
        return ResponseUtil.wrapOrNotFound(admissionTransferDTO);
    }

    /**
     * {@code DELETE  /admission-transfers/:id} : delete the "id" admissionTransfer.
     *
     * @param id the id of the admissionTransferDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAdmissionTransfer(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete AdmissionTransfer : {}", id);
        admissionTransferService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
