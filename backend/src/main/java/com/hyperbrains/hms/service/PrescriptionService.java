package com.hyperbrains.hms.service;

import com.hyperbrains.hms.service.dto.PrescriptionDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.hyperbrains.hms.domain.Prescription}.
 */
public interface PrescriptionService {
    /**
     * Save a prescription.
     *
     * @param prescriptionDTO the entity to save.
     * @return the persisted entity.
     */
    PrescriptionDTO save(PrescriptionDTO prescriptionDTO);

    /**
     * Updates a prescription.
     *
     * @param prescriptionDTO the entity to update.
     * @return the persisted entity.
     */
    PrescriptionDTO update(PrescriptionDTO prescriptionDTO);

    /**
     * Partially updates a prescription.
     *
     * @param prescriptionDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<PrescriptionDTO> partialUpdate(PrescriptionDTO prescriptionDTO);

    /**
     * Get all the prescriptions.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<PrescriptionDTO> findAll(Pageable pageable);

    /**
     * Get all the prescriptions with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<PrescriptionDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" prescription.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<PrescriptionDTO> findOne(Long id);

    /**
     * Delete the "id" prescription.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
