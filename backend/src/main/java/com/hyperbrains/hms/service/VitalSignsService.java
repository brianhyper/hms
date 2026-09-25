package com.hyperbrains.hms.service;

import com.hyperbrains.hms.service.dto.VitalSignsDTO;
import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link com.hyperbrains.hms.domain.VitalSigns}.
 */
public interface VitalSignsService {
    /**
     * Save a vitalSigns.
     *
     * @param vitalSignsDTO the entity to save.
     * @return the persisted entity.
     */
    VitalSignsDTO save(VitalSignsDTO vitalSignsDTO);

    /**
     * Updates a vitalSigns.
     *
     * @param vitalSignsDTO the entity to update.
     * @return the persisted entity.
     */
    VitalSignsDTO update(VitalSignsDTO vitalSignsDTO);

    /**
     * Partially updates a vitalSigns.
     *
     * @param vitalSignsDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<VitalSignsDTO> partialUpdate(VitalSignsDTO vitalSignsDTO);

    /**
     * Get all the vitalSignses.
     *
     * @return the list of entities.
     */
    List<VitalSignsDTO> findAll();

    /**
     * Get all the VitalSignsDTO where Visit is {@code null}.
     *
     * @return the {@link List} of entities.
     */
    List<VitalSignsDTO> findAllWhereVisitIsNull();

    /**
     * Get the "id" vitalSigns.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<VitalSignsDTO> findOne(Long id);

    /**
     * Delete the "id" vitalSigns.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
