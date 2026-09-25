package com.hyperbrains.hms.service;

import com.hyperbrains.hms.service.dto.AdmissionDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.hyperbrains.hms.domain.Admission}.
 */
public interface AdmissionService {
    /**
     * Save a admission.
     *
     * @param admissionDTO the entity to save.
     * @return the persisted entity.
     */
    AdmissionDTO save(AdmissionDTO admissionDTO);

    /**
     * Updates a admission.
     *
     * @param admissionDTO the entity to update.
     * @return the persisted entity.
     */
    AdmissionDTO update(AdmissionDTO admissionDTO);

    /**
     * Partially updates a admission.
     *
     * @param admissionDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<AdmissionDTO> partialUpdate(AdmissionDTO admissionDTO);

    /**
     * Get all the admissions.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<AdmissionDTO> findAll(Pageable pageable);

    /**
     * Get all the admissions with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<AdmissionDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" admission.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<AdmissionDTO> findOne(Long id);

    /**
     * Delete the "id" admission.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
