package com.hyperbrains.hms.service;

import com.hyperbrains.hms.service.dto.HospitalServiceDTO;
import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link com.hyperbrains.hms.domain.HospitalService}.
 */
public interface HospitalServiceService {
    /**
     * Save a hospitalService.
     *
     * @param hospitalServiceDTO the entity to save.
     * @return the persisted entity.
     */
    HospitalServiceDTO save(HospitalServiceDTO hospitalServiceDTO);

    /**
     * Updates a hospitalService.
     *
     * @param hospitalServiceDTO the entity to update.
     * @return the persisted entity.
     */
    HospitalServiceDTO update(HospitalServiceDTO hospitalServiceDTO);

    /**
     * Partially updates a hospitalService.
     *
     * @param hospitalServiceDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<HospitalServiceDTO> partialUpdate(HospitalServiceDTO hospitalServiceDTO);

    /**
     * Get all the hospitalServices.
     *
     * @return the list of entities.
     */
    List<HospitalServiceDTO> findAll();

    /**
     * Get the "id" hospitalService.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<HospitalServiceDTO> findOne(Long id);

    /**
     * Delete the "id" hospitalService.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
