package com.hyperbrains.hms.web.rest;

import com.hyperbrains.hms.repository.ShiftRepository;
import com.hyperbrains.hms.service.ShiftService;
import com.hyperbrains.hms.service.dto.ShiftDTO;
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
 * REST controller for managing {@link com.hyperbrains.hms.domain.Shift}.
 *
 * <p>There is deliberately no {@code DELETE}, for the same reason a staff record has none: the roster is the record
 * of who was on duty, and removing a day from it rewrites what happened. A shift that is wrong is corrected. If the
 * roster needs to take a future shift off, that is its own operation with its own rule about how far ahead it may do
 * so — and a missing route says "not this way" more honestly than a route that exists and refuses.
 *
 * <p>These are the escape-hatch writes: every one of them is Super Admin only in the RBAC table, because the roster
 * is about to become an access rule's source of truth and a raw write must not be able to move who was on duty.
 */
@RestController
@RequestMapping("/api/shifts")
public class ShiftResource {

    private static final Logger LOG = LoggerFactory.getLogger(ShiftResource.class);

    private static final String ENTITY_NAME = "shift";

    @Value("${jhipster.clientApp.name:hms}")
    private String applicationName;

    private final ShiftService shiftService;

    private final ShiftRepository shiftRepository;

    public ShiftResource(ShiftService shiftService, ShiftRepository shiftRepository) {
        this.shiftService = shiftService;
        this.shiftRepository = shiftRepository;
    }

    /**
     * {@code POST  /shifts} : Create a new shift.
     *
     * @param shiftDTO the shiftDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new shiftDTO.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<ShiftDTO> createShift(@Valid @RequestBody ShiftDTO shiftDTO) throws URISyntaxException {
        LOG.debug("REST request to save Shift : {}", shiftDTO);
        if (shiftDTO.getId() != null) {
            throw new BadRequestAlertException("A new shift cannot already have an ID", ENTITY_NAME, "idexists");
        }
        shiftDTO = shiftService.save(shiftDTO);
        return ResponseEntity.created(new URI("/api/shifts/" + shiftDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, shiftDTO.getId().toString()))
            .body(shiftDTO);
    }

    /**
     * {@code PUT  /shifts/:id} : Updates an existing shift.
     *
     * @param id the id of the shiftDTO to save.
     * @param shiftDTO the shiftDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated shiftDTO.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ShiftDTO> updateShift(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody ShiftDTO shiftDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Shift : {}, {}", id, shiftDTO);
        if (shiftDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, shiftDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }
        if (!shiftRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        shiftDTO = shiftService.update(shiftDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, shiftDTO.getId().toString()))
            .body(shiftDTO);
    }

    /**
     * {@code PATCH  /shifts/:id} : Partial update of an existing shift. Null fields are ignored.
     *
     * @param id the id of the shiftDTO to save.
     * @param shiftDTO the shiftDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated shiftDTO, or with
     *         status {@code 404 (Not Found)} if the shift is not found.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<ShiftDTO> partialUpdateShift(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody ShiftDTO shiftDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Shift partially : {}, {}", id, shiftDTO);
        if (shiftDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, shiftDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }
        if (!shiftRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<ShiftDTO> result = shiftService.partialUpdate(shiftDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, shiftDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /shifts} : get all the shifts.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of shifts in body.
     */
    @GetMapping("")
    public List<ShiftDTO> getAllShifts() {
        LOG.debug("REST request to get all Shifts");
        return shiftService.findAll();
    }

    /**
     * {@code GET  /shifts/:id} : get the "id" shift.
     *
     * @param id the id of the shiftDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the shiftDTO, or with status
     *         {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ShiftDTO> getShift(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Shift : {}", id);
        Optional<ShiftDTO> shiftDTO = shiftService.findOne(id);
        return ResponseUtil.wrapOrNotFound(shiftDTO);
    }
}
