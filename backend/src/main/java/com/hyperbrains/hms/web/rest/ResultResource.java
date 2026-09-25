package com.hyperbrains.hms.web.rest;

import com.hyperbrains.hms.repository.ResultRepository;
import com.hyperbrains.hms.service.ResultService;
import com.hyperbrains.hms.service.dto.ResultDTO;
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
 * REST controller for managing {@link com.hyperbrains.hms.domain.Result}.
 */
@RestController
@RequestMapping("/api/results")
public class ResultResource {

    private static final Logger LOG = LoggerFactory.getLogger(ResultResource.class);

    private static final String ENTITY_NAME = "result";

    @Value("${jhipster.clientApp.name:hms}")
    private String applicationName;

    private final ResultService resultService;

    private final ResultRepository resultRepository;

    public ResultResource(ResultService resultService, ResultRepository resultRepository) {
        this.resultService = resultService;
        this.resultRepository = resultRepository;
    }

    /**
     * {@code POST  /results} : Create a new result.
     *
     * @param resultDTO the resultDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new resultDTO, or with status {@code 400 (Bad Request)} if the result has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<ResultDTO> createResult(@Valid @RequestBody ResultDTO resultDTO) throws URISyntaxException {
        LOG.debug("REST request to save Result : {}", resultDTO);
        if (resultDTO.getId() != null) {
            throw new BadRequestAlertException("A new result cannot already have an ID", ENTITY_NAME, "idexists");
        }
        resultDTO = resultService.save(resultDTO);
        return ResponseEntity.created(new URI("/api/results/" + resultDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, resultDTO.getId().toString()))
            .body(resultDTO);
    }

    /**
     * {@code PUT  /results/:id} : Updates an existing result.
     *
     * @param id the id of the resultDTO to save.
     * @param resultDTO the resultDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated resultDTO,
     * or with status {@code 400 (Bad Request)} if the resultDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the resultDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ResultDTO> updateResult(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody ResultDTO resultDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Result : {}, {}", id, resultDTO);
        if (resultDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, resultDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!resultRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        resultDTO = resultService.update(resultDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, resultDTO.getId().toString()))
            .body(resultDTO);
    }

    /**
     * {@code PATCH  /results/:id} : Partial updates given fields of an existing result, field will ignore if it is null
     *
     * @param id the id of the resultDTO to save.
     * @param resultDTO the resultDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated resultDTO,
     * or with status {@code 400 (Bad Request)} if the resultDTO is not valid,
     * or with status {@code 404 (Not Found)} if the resultDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the resultDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<ResultDTO> partialUpdateResult(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody ResultDTO resultDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Result partially : {}, {}", id, resultDTO);
        if (resultDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, resultDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!resultRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<ResultDTO> result = resultService.partialUpdate(resultDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, resultDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /results} : get all the Results.
     *
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @param filter the filter of the request.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Results in body.
     */
    @GetMapping("")
    public List<ResultDTO> getAllResults(
        @RequestParam(name = "filter", required = false) String filter,
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        if ("order-is-null".equals(filter)) {
            LOG.debug("REST request to get all Results where order is null");
            return resultService.findAllWhereOrderIsNull();
        }
        LOG.debug("REST request to get all Results");
        return resultService.findAll();
    }

    /**
     * {@code GET  /results/:id} : get the "id" result.
     *
     * @param id the id of the resultDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the resultDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResultDTO> getResult(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Result : {}", id);
        Optional<ResultDTO> resultDTO = resultService.findOne(id);
        return ResponseUtil.wrapOrNotFound(resultDTO);
    }

    /**
     * {@code DELETE  /results/:id} : delete the "id" result.
     *
     * @param id the id of the resultDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResult(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Result : {}", id);
        resultService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
