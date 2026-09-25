package com.hyperbrains.hms.service;

import com.hyperbrains.hms.service.dto.RadiologyExamDTO;
import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link com.hyperbrains.hms.domain.RadiologyExam}.
 */
public interface RadiologyExamService {
    /**
     * Save a radiologyExam.
     *
     * @param radiologyExamDTO the entity to save.
     * @return the persisted entity.
     */
    RadiologyExamDTO save(RadiologyExamDTO radiologyExamDTO);

    /**
     * Updates a radiologyExam.
     *
     * @param radiologyExamDTO the entity to update.
     * @return the persisted entity.
     */
    RadiologyExamDTO update(RadiologyExamDTO radiologyExamDTO);

    /**
     * Partially updates a radiologyExam.
     *
     * @param radiologyExamDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<RadiologyExamDTO> partialUpdate(RadiologyExamDTO radiologyExamDTO);

    /**
     * Get all the radiologyExams.
     *
     * @return the list of entities.
     */
    List<RadiologyExamDTO> findAll();

    /**
     * Get the "id" radiologyExam.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<RadiologyExamDTO> findOne(Long id);

    /**
     * Delete the "id" radiologyExam.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
