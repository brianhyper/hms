package com.hyperbrains.hms.web.rest;

import com.hyperbrains.hms.repository.StaffRecordRepository;
import com.hyperbrains.hms.service.StaffRecordService;
import com.hyperbrains.hms.service.dto.StaffRecordDTO;
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
 * REST controller for managing {@link com.hyperbrains.hms.domain.StaffRecord}.
 *
 * <p>There is deliberately no {@code DELETE}. A staff record is a person's employment history and the parent of the
 * rostering, leave and payroll rows Phase 4 adds, so deleting one would either orphan those rows or cascade history
 * away. Somebody who has left is recorded as {@code TERMINATED} — the same choice the account side makes, where an
 * account is deactivated and never removed. A missing route is the honest way to say that: the alternative is a
 * route that exists and refuses, which invites the caller to keep trying.
 */
@RestController
@RequestMapping("/api/staff-records")
public class StaffRecordResource {

    private static final Logger LOG = LoggerFactory.getLogger(StaffRecordResource.class);

    private static final String ENTITY_NAME = "staffRecord";

    @Value("${jhipster.clientApp.name:hms}")
    private String applicationName;

    private final StaffRecordService staffRecordService;

    private final StaffRecordRepository staffRecordRepository;

    public StaffRecordResource(StaffRecordService staffRecordService, StaffRecordRepository staffRecordRepository) {
        this.staffRecordService = staffRecordService;
        this.staffRecordRepository = staffRecordRepository;
    }

    /**
     * {@code POST  /staff-records} : Create a new staff record.
     *
     * @param staffRecordDTO the staffRecordDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new staffRecordDTO, or
     *         with status {@code 409 (Conflict)} if the person is already on file or the account is already taken.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<StaffRecordDTO> createStaffRecord(@Valid @RequestBody StaffRecordDTO staffRecordDTO) throws URISyntaxException {
        LOG.debug("REST request to save StaffRecord : {}", staffRecordDTO);
        if (staffRecordDTO.getId() != null) {
            throw new BadRequestAlertException("A new staff record cannot already have an ID", ENTITY_NAME, "idexists");
        }
        staffRecordDTO = staffRecordService.save(staffRecordDTO);
        return ResponseEntity.created(new URI("/api/staff-records/" + staffRecordDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, staffRecordDTO.getId().toString()))
            .body(staffRecordDTO);
    }

    /**
     * {@code PUT  /staff-records/:id} : Updates an existing staff record.
     *
     * @param id the id of the staffRecordDTO to save.
     * @param staffRecordDTO the staffRecordDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated staffRecordDTO.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<StaffRecordDTO> updateStaffRecord(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody StaffRecordDTO staffRecordDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update StaffRecord : {}, {}", id, staffRecordDTO);
        if (staffRecordDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, staffRecordDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }
        if (!staffRecordRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        staffRecordDTO = staffRecordService.update(staffRecordDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, staffRecordDTO.getId().toString()))
            .body(staffRecordDTO);
    }

    /**
     * {@code PATCH  /staff-records/:id} : Partial update of an existing staff record. Null fields are ignored.
     *
     * @param id the id of the staffRecordDTO to save.
     * @param staffRecordDTO the staffRecordDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated staffRecordDTO, or
     *         with status {@code 404 (Not Found)} if the record is not found.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<StaffRecordDTO> partialUpdateStaffRecord(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody StaffRecordDTO staffRecordDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update StaffRecord partially : {}, {}", id, staffRecordDTO);
        if (staffRecordDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, staffRecordDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }
        if (!staffRecordRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<StaffRecordDTO> result = staffRecordService.partialUpdate(staffRecordDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, staffRecordDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /staff-records} : get all the staff records.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of staff records in body.
     */
    @GetMapping("")
    public List<StaffRecordDTO> getAllStaffRecords() {
        LOG.debug("REST request to get all StaffRecords");
        return staffRecordService.findAll();
    }

    /**
     * {@code GET  /staff-records/:id} : get the "id" staff record.
     *
     * @param id the id of the staffRecordDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the staffRecordDTO, or with
     *         status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<StaffRecordDTO> getStaffRecord(@PathVariable("id") Long id) {
        LOG.debug("REST request to get StaffRecord : {}", id);
        Optional<StaffRecordDTO> staffRecordDTO = staffRecordService.findOne(id);
        return ResponseUtil.wrapOrNotFound(staffRecordDTO);
    }
}
