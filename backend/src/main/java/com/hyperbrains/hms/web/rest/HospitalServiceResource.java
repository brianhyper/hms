package com.hyperbrains.hms.web.rest;

import com.hyperbrains.hms.repository.HospitalServiceRepository;
import com.hyperbrains.hms.service.HospitalServiceService;
import com.hyperbrains.hms.service.dto.HospitalServiceDTO;
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
 * REST controller for managing {@link com.hyperbrains.hms.domain.HospitalService}.
 */
@RestController
@RequestMapping("/api/hospital-services")
public class HospitalServiceResource {

    private static final Logger LOG = LoggerFactory.getLogger(HospitalServiceResource.class);

    private static final String ENTITY_NAME = "hospitalService";

    @Value("${jhipster.clientApp.name:hms}")
    private String applicationName;

    private final HospitalServiceService hospitalServiceService;

    private final HospitalServiceRepository hospitalServiceRepository;

    public HospitalServiceResource(HospitalServiceService hospitalServiceService, HospitalServiceRepository hospitalServiceRepository) {
        this.hospitalServiceService = hospitalServiceService;
        this.hospitalServiceRepository = hospitalServiceRepository;
    }

    /**
     * {@code POST  /hospital-services} : Create a new hospitalService.
     *
     * @param hospitalServiceDTO the hospitalServiceDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new hospitalServiceDTO, or with status {@code 400 (Bad Request)} if the hospitalService has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<HospitalServiceDTO> createHospitalService(@Valid @RequestBody HospitalServiceDTO hospitalServiceDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save HospitalService : {}", hospitalServiceDTO);
        if (hospitalServiceDTO.getId() != null) {
            throw new BadRequestAlertException("A new hospitalService cannot already have an ID", ENTITY_NAME, "idexists");
        }
        hospitalServiceDTO = hospitalServiceService.save(hospitalServiceDTO);
        return ResponseEntity.created(new URI("/api/hospital-services/" + hospitalServiceDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, hospitalServiceDTO.getId().toString()))
            .body(hospitalServiceDTO);
    }

    /**
     * {@code PUT  /hospital-services/:id} : Updates an existing hospitalService.
     *
     * @param id the id of the hospitalServiceDTO to save.
     * @param hospitalServiceDTO the hospitalServiceDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated hospitalServiceDTO,
     * or with status {@code 400 (Bad Request)} if the hospitalServiceDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the hospitalServiceDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<HospitalServiceDTO> updateHospitalService(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody HospitalServiceDTO hospitalServiceDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update HospitalService : {}, {}", id, hospitalServiceDTO);
        if (hospitalServiceDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, hospitalServiceDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!hospitalServiceRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        hospitalServiceDTO = hospitalServiceService.update(hospitalServiceDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, hospitalServiceDTO.getId().toString()))
            .body(hospitalServiceDTO);
    }

    /**
     * {@code PATCH  /hospital-services/:id} : Partial updates given fields of an existing hospitalService, field will ignore if it is null
     *
     * @param id the id of the hospitalServiceDTO to save.
     * @param hospitalServiceDTO the hospitalServiceDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated hospitalServiceDTO,
     * or with status {@code 400 (Bad Request)} if the hospitalServiceDTO is not valid,
     * or with status {@code 404 (Not Found)} if the hospitalServiceDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the hospitalServiceDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<HospitalServiceDTO> partialUpdateHospitalService(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody HospitalServiceDTO hospitalServiceDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update HospitalService partially : {}, {}", id, hospitalServiceDTO);
        if (hospitalServiceDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, hospitalServiceDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!hospitalServiceRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<HospitalServiceDTO> result = hospitalServiceService.partialUpdate(hospitalServiceDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, hospitalServiceDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /hospital-services} : get all the Hospital Services.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Hospital Services in body.
     */
    @GetMapping("")
    public List<HospitalServiceDTO> getAllHospitalServices() {
        LOG.debug("REST request to get all HospitalServices");
        return hospitalServiceService.findAll();
    }

    /**
     * {@code GET  /hospital-services/:id} : get the "id" hospitalService.
     *
     * @param id the id of the hospitalServiceDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the hospitalServiceDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<HospitalServiceDTO> getHospitalService(@PathVariable("id") Long id) {
        LOG.debug("REST request to get HospitalService : {}", id);
        Optional<HospitalServiceDTO> hospitalServiceDTO = hospitalServiceService.findOne(id);
        return ResponseUtil.wrapOrNotFound(hospitalServiceDTO);
    }

    /**
     * {@code DELETE  /hospital-services/:id} : delete the "id" hospitalService.
     *
     * @param id the id of the hospitalServiceDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHospitalService(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete HospitalService : {}", id);
        hospitalServiceService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
