package com.hyperbrains.hms.web.rest;

import com.hyperbrains.hms.repository.DiagnosticOrderRepository;
import com.hyperbrains.hms.service.DiagnosticOrderService;
import com.hyperbrains.hms.service.dto.DiagnosticOrderDTO;
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
 * REST controller for managing {@link com.hyperbrains.hms.domain.DiagnosticOrder}.
 */
@RestController
@RequestMapping("/api/diagnostic-orders")
public class DiagnosticOrderResource {

    private static final Logger LOG = LoggerFactory.getLogger(DiagnosticOrderResource.class);

    private static final String ENTITY_NAME = "diagnosticOrder";

    @Value("${jhipster.clientApp.name:hms}")
    private String applicationName;

    private final DiagnosticOrderService diagnosticOrderService;

    private final DiagnosticOrderRepository diagnosticOrderRepository;

    public DiagnosticOrderResource(DiagnosticOrderService diagnosticOrderService, DiagnosticOrderRepository diagnosticOrderRepository) {
        this.diagnosticOrderService = diagnosticOrderService;
        this.diagnosticOrderRepository = diagnosticOrderRepository;
    }

    /**
     * {@code POST  /diagnostic-orders} : Create a new diagnosticOrder.
     *
     * @param diagnosticOrderDTO the diagnosticOrderDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new diagnosticOrderDTO, or with status {@code 400 (Bad Request)} if the diagnosticOrder has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<DiagnosticOrderDTO> createDiagnosticOrder(@Valid @RequestBody DiagnosticOrderDTO diagnosticOrderDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save DiagnosticOrder : {}", diagnosticOrderDTO);
        if (diagnosticOrderDTO.getId() != null) {
            throw new BadRequestAlertException("A new diagnosticOrder cannot already have an ID", ENTITY_NAME, "idexists");
        }
        diagnosticOrderDTO = diagnosticOrderService.save(diagnosticOrderDTO);
        return ResponseEntity.created(new URI("/api/diagnostic-orders/" + diagnosticOrderDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, diagnosticOrderDTO.getId().toString()))
            .body(diagnosticOrderDTO);
    }

    /**
     * {@code PUT  /diagnostic-orders/:id} : Updates an existing diagnosticOrder.
     *
     * @param id the id of the diagnosticOrderDTO to save.
     * @param diagnosticOrderDTO the diagnosticOrderDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated diagnosticOrderDTO,
     * or with status {@code 400 (Bad Request)} if the diagnosticOrderDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the diagnosticOrderDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<DiagnosticOrderDTO> updateDiagnosticOrder(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody DiagnosticOrderDTO diagnosticOrderDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update DiagnosticOrder : {}, {}", id, diagnosticOrderDTO);
        if (diagnosticOrderDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, diagnosticOrderDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!diagnosticOrderRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        diagnosticOrderDTO = diagnosticOrderService.update(diagnosticOrderDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, diagnosticOrderDTO.getId().toString()))
            .body(diagnosticOrderDTO);
    }

    /**
     * {@code PATCH  /diagnostic-orders/:id} : Partial updates given fields of an existing diagnosticOrder, field will ignore if it is null
     *
     * @param id the id of the diagnosticOrderDTO to save.
     * @param diagnosticOrderDTO the diagnosticOrderDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated diagnosticOrderDTO,
     * or with status {@code 400 (Bad Request)} if the diagnosticOrderDTO is not valid,
     * or with status {@code 404 (Not Found)} if the diagnosticOrderDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the diagnosticOrderDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<DiagnosticOrderDTO> partialUpdateDiagnosticOrder(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody DiagnosticOrderDTO diagnosticOrderDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update DiagnosticOrder partially : {}, {}", id, diagnosticOrderDTO);
        if (diagnosticOrderDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, diagnosticOrderDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!diagnosticOrderRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<DiagnosticOrderDTO> result = diagnosticOrderService.partialUpdate(diagnosticOrderDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, diagnosticOrderDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /diagnostic-orders} : get all the Diagnostic Orders.
     *
     * @param pageable the pagination information.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Diagnostic Orders in body.
     */
    @GetMapping("")
    public ResponseEntity<List<DiagnosticOrderDTO>> getAllDiagnosticOrders(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to get a page of DiagnosticOrders");
        Page<DiagnosticOrderDTO> page;
        if (eagerload) {
            page = diagnosticOrderService.findAllWithEagerRelationships(pageable);
        } else {
            page = diagnosticOrderService.findAll(pageable);
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /diagnostic-orders/:id} : get the "id" diagnosticOrder.
     *
     * @param id the id of the diagnosticOrderDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the diagnosticOrderDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<DiagnosticOrderDTO> getDiagnosticOrder(@PathVariable("id") Long id) {
        LOG.debug("REST request to get DiagnosticOrder : {}", id);
        Optional<DiagnosticOrderDTO> diagnosticOrderDTO = diagnosticOrderService.findOne(id);
        return ResponseUtil.wrapOrNotFound(diagnosticOrderDTO);
    }

    /**
     * {@code DELETE  /diagnostic-orders/:id} : delete the "id" diagnosticOrder.
     *
     * @param id the id of the diagnosticOrderDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDiagnosticOrder(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete DiagnosticOrder : {}", id);
        diagnosticOrderService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
