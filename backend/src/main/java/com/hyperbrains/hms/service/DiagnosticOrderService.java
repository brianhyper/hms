package com.hyperbrains.hms.service;

import com.hyperbrains.hms.service.dto.DiagnosticOrderDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.hyperbrains.hms.domain.DiagnosticOrder}.
 */
public interface DiagnosticOrderService {
    /**
     * Save a diagnosticOrder.
     *
     * @param diagnosticOrderDTO the entity to save.
     * @return the persisted entity.
     */
    DiagnosticOrderDTO save(DiagnosticOrderDTO diagnosticOrderDTO);

    /**
     * Updates a diagnosticOrder.
     *
     * @param diagnosticOrderDTO the entity to update.
     * @return the persisted entity.
     */
    DiagnosticOrderDTO update(DiagnosticOrderDTO diagnosticOrderDTO);

    /**
     * Partially updates a diagnosticOrder.
     *
     * @param diagnosticOrderDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<DiagnosticOrderDTO> partialUpdate(DiagnosticOrderDTO diagnosticOrderDTO);

    /**
     * Get all the diagnosticOrders.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<DiagnosticOrderDTO> findAll(Pageable pageable);

    /**
     * Get all the diagnosticOrders with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<DiagnosticOrderDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" diagnosticOrder.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<DiagnosticOrderDTO> findOne(Long id);

    /**
     * Delete the "id" diagnosticOrder.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
