package com.hyperbrains.hms.service;

import com.hyperbrains.hms.service.dto.BedDTO;
import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link com.hyperbrains.hms.domain.Bed}.
 */
public interface BedService {
    /**
     * Save a bed.
     *
     * @param bedDTO the entity to save.
     * @return the persisted entity.
     */
    BedDTO save(BedDTO bedDTO);

    /**
     * Updates a bed.
     *
     * @param bedDTO the entity to update.
     * @return the persisted entity.
     */
    BedDTO update(BedDTO bedDTO);

    /**
     * Partially updates a bed.
     *
     * @param bedDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<BedDTO> partialUpdate(BedDTO bedDTO);

    /**
     * Get all the beds.
     *
     * @return the list of entities.
     */
    List<BedDTO> findAll();

    /**
     * Get the "id" bed.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<BedDTO> findOne(Long id);

    /**
     * Delete the "id" bed.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
