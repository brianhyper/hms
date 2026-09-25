package com.hyperbrains.hms.service;

import com.hyperbrains.hms.service.dto.DiagnosisDTO;
import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link com.hyperbrains.hms.domain.Diagnosis}.
 */
public interface DiagnosisService {
    /**
     * Save a diagnosis.
     *
     * @param diagnosisDTO the entity to save.
     * @return the persisted entity.
     */
    DiagnosisDTO save(DiagnosisDTO diagnosisDTO);

    /**
     * Updates a diagnosis.
     *
     * @param diagnosisDTO the entity to update.
     * @return the persisted entity.
     */
    DiagnosisDTO update(DiagnosisDTO diagnosisDTO);

    /**
     * Partially updates a diagnosis.
     *
     * @param diagnosisDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<DiagnosisDTO> partialUpdate(DiagnosisDTO diagnosisDTO);

    /**
     * Get all the diagnoses.
     *
     * @return the list of entities.
     */
    List<DiagnosisDTO> findAll();

    /**
     * Get the "id" diagnosis.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<DiagnosisDTO> findOne(Long id);

    /**
     * Delete the "id" diagnosis.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
