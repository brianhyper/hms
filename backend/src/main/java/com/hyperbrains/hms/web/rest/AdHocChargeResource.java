package com.hyperbrains.hms.web.rest;

import com.hyperbrains.hms.repository.AdHocChargeRepository;
import com.hyperbrains.hms.service.AdHocChargeService;
import com.hyperbrains.hms.service.dto.AdHocChargeDTO;
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
 * REST controller for managing {@link com.hyperbrains.hms.domain.AdHocCharge}.
 */
@RestController
@RequestMapping("/api/ad-hoc-charges")
public class AdHocChargeResource {

    private static final Logger LOG = LoggerFactory.getLogger(AdHocChargeResource.class);

    private static final String ENTITY_NAME = "adHocCharge";

    @Value("${jhipster.clientApp.name:hms}")
    private String applicationName;

    private final AdHocChargeService adHocChargeService;

    private final AdHocChargeRepository adHocChargeRepository;

    public AdHocChargeResource(AdHocChargeService adHocChargeService, AdHocChargeRepository adHocChargeRepository) {
        this.adHocChargeService = adHocChargeService;
        this.adHocChargeRepository = adHocChargeRepository;
    }

    /**
     * {@code POST  /ad-hoc-charges} : Create a new adHocCharge.
     *
     * @param adHocChargeDTO the adHocChargeDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new adHocChargeDTO, or with status {@code 400 (Bad Request)} if the adHocCharge has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<AdHocChargeDTO> createAdHocCharge(@Valid @RequestBody AdHocChargeDTO adHocChargeDTO) throws URISyntaxException {
        LOG.debug("REST request to save AdHocCharge : {}", adHocChargeDTO);
        if (adHocChargeDTO.getId() != null) {
            throw new BadRequestAlertException("A new adHocCharge cannot already have an ID", ENTITY_NAME, "idexists");
        }
        adHocChargeDTO = adHocChargeService.save(adHocChargeDTO);
        return ResponseEntity.created(new URI("/api/ad-hoc-charges/" + adHocChargeDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, adHocChargeDTO.getId().toString()))
            .body(adHocChargeDTO);
    }

    /**
     * {@code PUT  /ad-hoc-charges/:id} : Updates an existing adHocCharge.
     *
     * @param id the id of the adHocChargeDTO to save.
     * @param adHocChargeDTO the adHocChargeDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated adHocChargeDTO,
     * or with status {@code 400 (Bad Request)} if the adHocChargeDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the adHocChargeDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<AdHocChargeDTO> updateAdHocCharge(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody AdHocChargeDTO adHocChargeDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update AdHocCharge : {}, {}", id, adHocChargeDTO);
        if (adHocChargeDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, adHocChargeDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!adHocChargeRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        adHocChargeDTO = adHocChargeService.update(adHocChargeDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, adHocChargeDTO.getId().toString()))
            .body(adHocChargeDTO);
    }

    /**
     * {@code PATCH  /ad-hoc-charges/:id} : Partial updates given fields of an existing adHocCharge, field will ignore if it is null
     *
     * @param id the id of the adHocChargeDTO to save.
     * @param adHocChargeDTO the adHocChargeDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated adHocChargeDTO,
     * or with status {@code 400 (Bad Request)} if the adHocChargeDTO is not valid,
     * or with status {@code 404 (Not Found)} if the adHocChargeDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the adHocChargeDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<AdHocChargeDTO> partialUpdateAdHocCharge(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody AdHocChargeDTO adHocChargeDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update AdHocCharge partially : {}, {}", id, adHocChargeDTO);
        if (adHocChargeDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, adHocChargeDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!adHocChargeRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<AdHocChargeDTO> result = adHocChargeService.partialUpdate(adHocChargeDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, adHocChargeDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /ad-hoc-charges} : get all the Ad Hoc Charges.
     *
     * @param pageable the pagination information.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Ad Hoc Charges in body.
     */
    @GetMapping("")
    public ResponseEntity<List<AdHocChargeDTO>> getAllAdHocCharges(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to get a page of AdHocCharges");
        Page<AdHocChargeDTO> page;
        if (eagerload) {
            page = adHocChargeService.findAllWithEagerRelationships(pageable);
        } else {
            page = adHocChargeService.findAll(pageable);
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /ad-hoc-charges/:id} : get the "id" adHocCharge.
     *
     * @param id the id of the adHocChargeDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the adHocChargeDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AdHocChargeDTO> getAdHocCharge(@PathVariable("id") Long id) {
        LOG.debug("REST request to get AdHocCharge : {}", id);
        Optional<AdHocChargeDTO> adHocChargeDTO = adHocChargeService.findOne(id);
        return ResponseUtil.wrapOrNotFound(adHocChargeDTO);
    }

    /**
     * {@code DELETE  /ad-hoc-charges/:id} : delete the "id" adHocCharge.
     *
     * @param id the id of the adHocChargeDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAdHocCharge(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete AdHocCharge : {}", id);
        adHocChargeService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
