package com.hyperbrains.hms.service;

import com.hyperbrains.hms.service.dto.LabTestDTO;
import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link com.hyperbrains.hms.domain.LabTest}.
 */
public interface LabTestService {
    /**
     * Save a labTest.
     *
     * @param labTestDTO the entity to save.
     * @return the persisted entity.
     */
    LabTestDTO save(LabTestDTO labTestDTO);

    /**
     * Updates a labTest.
     *
     * @param labTestDTO the entity to update.
     * @return the persisted entity.
     */
    LabTestDTO update(LabTestDTO labTestDTO);

    /**
     * Partially updates a labTest.
     *
     * @param labTestDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<LabTestDTO> partialUpdate(LabTestDTO labTestDTO);

    /**
     * Get all the labTests.
     *
     * @return the list of entities.
     */
    List<LabTestDTO> findAll();

    /**
     * Get the "id" labTest.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<LabTestDTO> findOne(Long id);

    /**
     * Delete the "id" labTest.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
