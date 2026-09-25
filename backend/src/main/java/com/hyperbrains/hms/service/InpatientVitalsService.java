package com.hyperbrains.hms.service;

import com.hyperbrains.hms.service.dto.InpatientVitalsDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.hyperbrains.hms.domain.InpatientVitals}.
 */
public interface InpatientVitalsService {
    /**
     * Save a inpatientVitals.
     *
     * @param inpatientVitalsDTO the entity to save.
     * @return the persisted entity.
     */
    InpatientVitalsDTO save(InpatientVitalsDTO inpatientVitalsDTO);

    /**
     * Updates a inpatientVitals.
     *
     * @param inpatientVitalsDTO the entity to update.
     * @return the persisted entity.
     */
    InpatientVitalsDTO update(InpatientVitalsDTO inpatientVitalsDTO);

    /**
     * Partially updates a inpatientVitals.
     *
     * @param inpatientVitalsDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<InpatientVitalsDTO> partialUpdate(InpatientVitalsDTO inpatientVitalsDTO);

    /**
     * Get all the inpatientVitalses.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<InpatientVitalsDTO> findAll(Pageable pageable);

    /**
     * Get all the inpatientVitalses with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<InpatientVitalsDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" inpatientVitals.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<InpatientVitalsDTO> findOne(Long id);

    /**
     * Delete the "id" inpatientVitals.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
