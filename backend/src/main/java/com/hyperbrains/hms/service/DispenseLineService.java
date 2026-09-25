package com.hyperbrains.hms.service;

import com.hyperbrains.hms.service.dto.DispenseLineDTO;
import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link com.hyperbrains.hms.domain.DispenseLine}.
 */
public interface DispenseLineService {
    /**
     * Save a dispenseLine.
     *
     * @param dispenseLineDTO the entity to save.
     * @return the persisted entity.
     */
    DispenseLineDTO save(DispenseLineDTO dispenseLineDTO);

    /**
     * Updates a dispenseLine.
     *
     * @param dispenseLineDTO the entity to update.
     * @return the persisted entity.
     */
    DispenseLineDTO update(DispenseLineDTO dispenseLineDTO);

    /**
     * Partially updates a dispenseLine.
     *
     * @param dispenseLineDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<DispenseLineDTO> partialUpdate(DispenseLineDTO dispenseLineDTO);

    /**
     * Get all the dispenseLines.
     *
     * @return the list of entities.
     */
    List<DispenseLineDTO> findAll();

    /**
     * Get the "id" dispenseLine.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<DispenseLineDTO> findOne(Long id);

    /**
     * Delete the "id" dispenseLine.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
