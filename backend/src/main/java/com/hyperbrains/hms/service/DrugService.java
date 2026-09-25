package com.hyperbrains.hms.service;

import com.hyperbrains.hms.service.dto.DrugDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.hyperbrains.hms.domain.Drug}.
 */
public interface DrugService {
    /**
     * Save a drug.
     *
     * @param drugDTO the entity to save.
     * @return the persisted entity.
     */
    DrugDTO save(DrugDTO drugDTO);

    /**
     * Updates a drug.
     *
     * @param drugDTO the entity to update.
     * @return the persisted entity.
     */
    DrugDTO update(DrugDTO drugDTO);

    /**
     * Partially updates a drug.
     *
     * @param drugDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<DrugDTO> partialUpdate(DrugDTO drugDTO);

    /**
     * Get all the drugs.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<DrugDTO> findAll(Pageable pageable);

    /**
     * Get the "id" drug.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<DrugDTO> findOne(Long id);

    /**
     * Delete the "id" drug.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
