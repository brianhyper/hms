package com.hyperbrains.hms.service;

import com.hyperbrains.hms.service.dto.ResultDTO;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.hyperbrains.hms.domain.Result}.
 */
public interface ResultService {
    /**
     * Save a result.
     *
     * @param resultDTO the entity to save.
     * @return the persisted entity.
     */
    ResultDTO save(ResultDTO resultDTO);

    /**
     * Updates a result.
     *
     * @param resultDTO the entity to update.
     * @return the persisted entity.
     */
    ResultDTO update(ResultDTO resultDTO);

    /**
     * Partially updates a result.
     *
     * @param resultDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<ResultDTO> partialUpdate(ResultDTO resultDTO);

    /**
     * Get all the results.
     *
     * @return the list of entities.
     */
    List<ResultDTO> findAll();

    /**
     * Get all the ResultDTO where Order is {@code null}.
     *
     * @return the {@link List} of entities.
     */
    List<ResultDTO> findAllWhereOrderIsNull();

    /**
     * Get all the results with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<ResultDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" result.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<ResultDTO> findOne(Long id);

    /**
     * Delete the "id" result.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
