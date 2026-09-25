package com.hyperbrains.hms.web.rest;

import com.hyperbrains.hms.repository.ReferralRepository;
import com.hyperbrains.hms.service.ReferralService;
import com.hyperbrains.hms.service.dto.ReferralDTO;
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
 * REST controller for managing {@link com.hyperbrains.hms.domain.Referral}.
 */
@RestController
@RequestMapping("/api/referrals")
public class ReferralResource {

    private static final Logger LOG = LoggerFactory.getLogger(ReferralResource.class);

    private static final String ENTITY_NAME = "referral";

    @Value("${jhipster.clientApp.name:hms}")
    private String applicationName;

    private final ReferralService referralService;

    private final ReferralRepository referralRepository;

    public ReferralResource(ReferralService referralService, ReferralRepository referralRepository) {
        this.referralService = referralService;
        this.referralRepository = referralRepository;
    }

    /**
     * {@code POST  /referrals} : Create a new referral.
     *
     * @param referralDTO the referralDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new referralDTO, or with status {@code 400 (Bad Request)} if the referral has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<ReferralDTO> createReferral(@Valid @RequestBody ReferralDTO referralDTO) throws URISyntaxException {
        LOG.debug("REST request to save Referral : {}", referralDTO);
        if (referralDTO.getId() != null) {
            throw new BadRequestAlertException("A new referral cannot already have an ID", ENTITY_NAME, "idexists");
        }
        referralDTO = referralService.save(referralDTO);
        return ResponseEntity.created(new URI("/api/referrals/" + referralDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, referralDTO.getId().toString()))
            .body(referralDTO);
    }

    /**
     * {@code PUT  /referrals/:id} : Updates an existing referral.
     *
     * @param id the id of the referralDTO to save.
     * @param referralDTO the referralDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated referralDTO,
     * or with status {@code 400 (Bad Request)} if the referralDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the referralDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ReferralDTO> updateReferral(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody ReferralDTO referralDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Referral : {}, {}", id, referralDTO);
        if (referralDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, referralDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!referralRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        referralDTO = referralService.update(referralDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, referralDTO.getId().toString()))
            .body(referralDTO);
    }

    /**
     * {@code PATCH  /referrals/:id} : Partial updates given fields of an existing referral, field will ignore if it is null
     *
     * @param id the id of the referralDTO to save.
     * @param referralDTO the referralDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated referralDTO,
     * or with status {@code 400 (Bad Request)} if the referralDTO is not valid,
     * or with status {@code 404 (Not Found)} if the referralDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the referralDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<ReferralDTO> partialUpdateReferral(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody ReferralDTO referralDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Referral partially : {}, {}", id, referralDTO);
        if (referralDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, referralDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!referralRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<ReferralDTO> result = referralService.partialUpdate(referralDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, referralDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /referrals} : get all the Referrals.
     *
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Referrals in body.
     */
    @GetMapping("")
    public List<ReferralDTO> getAllReferrals(@RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload) {
        LOG.debug("REST request to get all Referrals");
        return referralService.findAll();
    }

    /**
     * {@code GET  /referrals/:id} : get the "id" referral.
     *
     * @param id the id of the referralDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the referralDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReferralDTO> getReferral(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Referral : {}", id);
        Optional<ReferralDTO> referralDTO = referralService.findOne(id);
        return ResponseUtil.wrapOrNotFound(referralDTO);
    }

    /**
     * {@code DELETE  /referrals/:id} : delete the "id" referral.
     *
     * @param id the id of the referralDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReferral(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Referral : {}", id);
        referralService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
