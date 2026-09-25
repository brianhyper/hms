package com.hyperbrains.hms.service;

import com.hyperbrains.hms.service.dto.OrderExecutionDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.hyperbrains.hms.domain.OrderExecution}.
 */
public interface OrderExecutionService {
    /**
     * Save a orderExecution.
     *
     * @param orderExecutionDTO the entity to save.
     * @return the persisted entity.
     */
    OrderExecutionDTO save(OrderExecutionDTO orderExecutionDTO);

    /**
     * Updates a orderExecution.
     *
     * @param orderExecutionDTO the entity to update.
     * @return the persisted entity.
     */
    OrderExecutionDTO update(OrderExecutionDTO orderExecutionDTO);

    /**
     * Partially updates a orderExecution.
     *
     * @param orderExecutionDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<OrderExecutionDTO> partialUpdate(OrderExecutionDTO orderExecutionDTO);

    /**
     * Get all the orderExecutions.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<OrderExecutionDTO> findAll(Pageable pageable);

    /**
     * Get all the orderExecutions with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<OrderExecutionDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" orderExecution.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<OrderExecutionDTO> findOne(Long id);

    /**
     * Delete the "id" orderExecution.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
