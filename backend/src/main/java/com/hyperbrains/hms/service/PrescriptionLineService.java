package com.hyperbrains.hms.service;

import com.hyperbrains.hms.service.dto.PrescriptionLineDTO;
import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link com.hyperbrains.hms.domain.PrescriptionLine}.
 */
public interface PrescriptionLineService {
    /**
     * Save a prescriptionLine.
     *
     * @param prescriptionLineDTO the entity to save.
     * @return the persisted entity.
     */
    PrescriptionLineDTO save(PrescriptionLineDTO prescriptionLineDTO);

    /**
     * Updates a prescriptionLine.
     *
     * @param prescriptionLineDTO the entity to update.
     * @return the persisted entity.
     */
    PrescriptionLineDTO update(PrescriptionLineDTO prescriptionLineDTO);

    /**
     * Partially updates a prescriptionLine.
     *
     * @param prescriptionLineDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<PrescriptionLineDTO> partialUpdate(PrescriptionLineDTO prescriptionLineDTO);

    /**
     * Get all the prescriptionLines.
     *
     * @return the list of entities.
     */
    List<PrescriptionLineDTO> findAll();

    /**
     * Get the "id" prescriptionLine.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<PrescriptionLineDTO> findOne(Long id);

    /**
     * Delete the "id" prescriptionLine.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
