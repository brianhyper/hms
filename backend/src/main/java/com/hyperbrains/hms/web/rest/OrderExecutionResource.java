package com.hyperbrains.hms.web.rest;

import com.hyperbrains.hms.repository.OrderExecutionRepository;
import com.hyperbrains.hms.service.OrderExecutionService;
import com.hyperbrains.hms.service.dto.OrderExecutionDTO;
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
 * REST controller for managing {@link com.hyperbrains.hms.domain.OrderExecution}.
 */
@RestController
@RequestMapping("/api/order-executions")
public class OrderExecutionResource {

    private static final Logger LOG = LoggerFactory.getLogger(OrderExecutionResource.class);

    private static final String ENTITY_NAME = "orderExecution";

    @Value("${jhipster.clientApp.name:hms}")
    private String applicationName;

    private final OrderExecutionService orderExecutionService;

    private final OrderExecutionRepository orderExecutionRepository;

    public OrderExecutionResource(OrderExecutionService orderExecutionService, OrderExecutionRepository orderExecutionRepository) {
        this.orderExecutionService = orderExecutionService;
        this.orderExecutionRepository = orderExecutionRepository;
    }

    /**
     * {@code POST  /order-executions} : Create a new orderExecution.
     *
     * @param orderExecutionDTO the orderExecutionDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new orderExecutionDTO, or with status {@code 400 (Bad Request)} if the orderExecution has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<OrderExecutionDTO> createOrderExecution(@Valid @RequestBody OrderExecutionDTO orderExecutionDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save OrderExecution : {}", orderExecutionDTO);
        if (orderExecutionDTO.getId() != null) {
            throw new BadRequestAlertException("A new orderExecution cannot already have an ID", ENTITY_NAME, "idexists");
        }
        orderExecutionDTO = orderExecutionService.save(orderExecutionDTO);
        return ResponseEntity.created(new URI("/api/order-executions/" + orderExecutionDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, orderExecutionDTO.getId().toString()))
            .body(orderExecutionDTO);
    }

    /**
     * {@code PUT  /order-executions/:id} : Updates an existing orderExecution.
     *
     * @param id the id of the orderExecutionDTO to save.
     * @param orderExecutionDTO the orderExecutionDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated orderExecutionDTO,
     * or with status {@code 400 (Bad Request)} if the orderExecutionDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the orderExecutionDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<OrderExecutionDTO> updateOrderExecution(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody OrderExecutionDTO orderExecutionDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update OrderExecution : {}, {}", id, orderExecutionDTO);
        if (orderExecutionDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, orderExecutionDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!orderExecutionRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        orderExecutionDTO = orderExecutionService.update(orderExecutionDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, orderExecutionDTO.getId().toString()))
            .body(orderExecutionDTO);
    }

    /**
     * {@code PATCH  /order-executions/:id} : Partial updates given fields of an existing orderExecution, field will ignore if it is null
     *
     * @param id the id of the orderExecutionDTO to save.
     * @param orderExecutionDTO the orderExecutionDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated orderExecutionDTO,
     * or with status {@code 400 (Bad Request)} if the orderExecutionDTO is not valid,
     * or with status {@code 404 (Not Found)} if the orderExecutionDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the orderExecutionDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<OrderExecutionDTO> partialUpdateOrderExecution(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody OrderExecutionDTO orderExecutionDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update OrderExecution partially : {}, {}", id, orderExecutionDTO);
        if (orderExecutionDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, orderExecutionDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!orderExecutionRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<OrderExecutionDTO> result = orderExecutionService.partialUpdate(orderExecutionDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, orderExecutionDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /order-executions} : get all the Order Executions.
     *
     * @param pageable the pagination information.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Order Executions in body.
     */
    @GetMapping("")
    public ResponseEntity<List<OrderExecutionDTO>> getAllOrderExecutions(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to get a page of OrderExecutions");
        Page<OrderExecutionDTO> page;
        if (eagerload) {
            page = orderExecutionService.findAllWithEagerRelationships(pageable);
        } else {
            page = orderExecutionService.findAll(pageable);
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /order-executions/:id} : get the "id" orderExecution.
     *
     * @param id the id of the orderExecutionDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the orderExecutionDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderExecutionDTO> getOrderExecution(@PathVariable("id") Long id) {
        LOG.debug("REST request to get OrderExecution : {}", id);
        Optional<OrderExecutionDTO> orderExecutionDTO = orderExecutionService.findOne(id);
        return ResponseUtil.wrapOrNotFound(orderExecutionDTO);
    }

    /**
     * {@code DELETE  /order-executions/:id} : delete the "id" orderExecution.
     *
     * @param id the id of the orderExecutionDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrderExecution(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete OrderExecution : {}", id);
        orderExecutionService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
