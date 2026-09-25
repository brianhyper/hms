package com.hyperbrains.hms.service;

import com.hyperbrains.hms.service.dto.BedTypeDTO;
import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link com.hyperbrains.hms.domain.BedType}.
 */
public interface BedTypeService {
    /**
     * Save a bedType.
     *
     * @param bedTypeDTO the entity to save.
     * @return the persisted entity.
     */
    BedTypeDTO save(BedTypeDTO bedTypeDTO);

    /**
     * Updates a bedType.
     *
     * @param bedTypeDTO the entity to update.
     * @return the persisted entity.
     */
    BedTypeDTO update(BedTypeDTO bedTypeDTO);

    /**
     * Partially updates a bedType.
     *
     * @param bedTypeDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<BedTypeDTO> partialUpdate(BedTypeDTO bedTypeDTO);

    /**
     * Get all the bedTypes.
     *
     * @return the list of entities.
     */
    List<BedTypeDTO> findAll();

    /**
     * Get the "id" bedType.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<BedTypeDTO> findOne(Long id);

    /**
     * Delete the "id" bedType.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
