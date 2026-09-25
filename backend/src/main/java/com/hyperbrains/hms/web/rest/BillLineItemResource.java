package com.hyperbrains.hms.web.rest;

import com.hyperbrains.hms.repository.BillLineItemRepository;
import com.hyperbrains.hms.service.BillLineItemService;
import com.hyperbrains.hms.service.dto.BillLineItemDTO;
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
 * REST controller for managing {@link com.hyperbrains.hms.domain.BillLineItem}.
 */
@RestController
@RequestMapping("/api/bill-line-items")
public class BillLineItemResource {

    private static final Logger LOG = LoggerFactory.getLogger(BillLineItemResource.class);

    private static final String ENTITY_NAME = "billLineItem";

    @Value("${jhipster.clientApp.name:hms}")
    private String applicationName;

    private final BillLineItemService billLineItemService;

    private final BillLineItemRepository billLineItemRepository;

    public BillLineItemResource(BillLineItemService billLineItemService, BillLineItemRepository billLineItemRepository) {
        this.billLineItemService = billLineItemService;
        this.billLineItemRepository = billLineItemRepository;
    }

    /**
     * {@code POST  /bill-line-items} : Create a new billLineItem.
     *
     * @param billLineItemDTO the billLineItemDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new billLineItemDTO, or with status {@code 400 (Bad Request)} if the billLineItem has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<BillLineItemDTO> createBillLineItem(@Valid @RequestBody BillLineItemDTO billLineItemDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save BillLineItem : {}", billLineItemDTO);
        if (billLineItemDTO.getId() != null) {
            throw new BadRequestAlertException("A new billLineItem cannot already have an ID", ENTITY_NAME, "idexists");
        }
        billLineItemDTO = billLineItemService.save(billLineItemDTO);
        return ResponseEntity.created(new URI("/api/bill-line-items/" + billLineItemDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, billLineItemDTO.getId().toString()))
            .body(billLineItemDTO);
    }

    /**
     * {@code PUT  /bill-line-items/:id} : Updates an existing billLineItem.
     *
     * @param id the id of the billLineItemDTO to save.
     * @param billLineItemDTO the billLineItemDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated billLineItemDTO,
     * or with status {@code 400 (Bad Request)} if the billLineItemDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the billLineItemDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<BillLineItemDTO> updateBillLineItem(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody BillLineItemDTO billLineItemDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update BillLineItem : {}, {}", id, billLineItemDTO);
        if (billLineItemDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, billLineItemDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!billLineItemRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        billLineItemDTO = billLineItemService.update(billLineItemDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, billLineItemDTO.getId().toString()))
            .body(billLineItemDTO);
    }

    /**
     * {@code PATCH  /bill-line-items/:id} : Partial updates given fields of an existing billLineItem, field will ignore if it is null
     *
     * @param id the id of the billLineItemDTO to save.
     * @param billLineItemDTO the billLineItemDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated billLineItemDTO,
     * or with status {@code 400 (Bad Request)} if the billLineItemDTO is not valid,
     * or with status {@code 404 (Not Found)} if the billLineItemDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the billLineItemDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<BillLineItemDTO> partialUpdateBillLineItem(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody BillLineItemDTO billLineItemDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update BillLineItem partially : {}, {}", id, billLineItemDTO);
        if (billLineItemDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, billLineItemDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!billLineItemRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<BillLineItemDTO> result = billLineItemService.partialUpdate(billLineItemDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, billLineItemDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /bill-line-items} : get all the Bill Line Items.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Bill Line Items in body.
     */
    @GetMapping("")
    public List<BillLineItemDTO> getAllBillLineItems() {
        LOG.debug("REST request to get all BillLineItems");
        return billLineItemService.findAll();
    }

    /**
     * {@code GET  /bill-line-items/:id} : get the "id" billLineItem.
     *
     * @param id the id of the billLineItemDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the billLineItemDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<BillLineItemDTO> getBillLineItem(@PathVariable("id") Long id) {
        LOG.debug("REST request to get BillLineItem : {}", id);
        Optional<BillLineItemDTO> billLineItemDTO = billLineItemService.findOne(id);
        return ResponseUtil.wrapOrNotFound(billLineItemDTO);
    }

    /**
     * {@code DELETE  /bill-line-items/:id} : delete the "id" billLineItem.
     *
     * @param id the id of the billLineItemDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBillLineItem(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete BillLineItem : {}", id);
        billLineItemService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
