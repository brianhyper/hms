package com.hyperbrains.hms.service;

import com.hyperbrains.hms.service.dto.AdmissionTransferDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.hyperbrains.hms.domain.AdmissionTransfer}.
 */
public interface AdmissionTransferService {
    /**
     * Save a admissionTransfer.
     *
     * @param admissionTransferDTO the entity to save.
     * @return the persisted entity.
     */
    AdmissionTransferDTO save(AdmissionTransferDTO admissionTransferDTO);

    /**
     * Updates a admissionTransfer.
     *
     * @param admissionTransferDTO the entity to update.
     * @return the persisted entity.
     */
    AdmissionTransferDTO update(AdmissionTransferDTO admissionTransferDTO);

    /**
     * Partially updates a admissionTransfer.
     *
     * @param admissionTransferDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<AdmissionTransferDTO> partialUpdate(AdmissionTransferDTO admissionTransferDTO);

    /**
     * Get all the admissionTransfers.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<AdmissionTransferDTO> findAll(Pageable pageable);

    /**
     * Get all the admissionTransfers with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<AdmissionTransferDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" admissionTransfer.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<AdmissionTransferDTO> findOne(Long id);

    /**
     * Delete the "id" admissionTransfer.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
