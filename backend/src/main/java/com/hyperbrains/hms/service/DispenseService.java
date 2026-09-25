package com.hyperbrains.hms.service;

import com.hyperbrains.hms.service.dto.DispenseDTO;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.hyperbrains.hms.domain.Dispense}.
 */
public interface DispenseService {
    /**
     * Save a dispense.
     *
     * @param dispenseDTO the entity to save.
     * @return the persisted entity.
     */
    DispenseDTO save(DispenseDTO dispenseDTO);

    /**
     * Updates a dispense.
     *
     * @param dispenseDTO the entity to update.
     * @return the persisted entity.
     */
    DispenseDTO update(DispenseDTO dispenseDTO);

    /**
     * Partially updates a dispense.
     *
     * @param dispenseDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<DispenseDTO> partialUpdate(DispenseDTO dispenseDTO);

    /**
     * Get all the dispenses.
     *
     * @return the list of entities.
     */
    List<DispenseDTO> findAll();

    /**
     * Get all the dispenses with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<DispenseDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" dispense.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<DispenseDTO> findOne(Long id);

    /**
     * Delete the "id" dispense.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
