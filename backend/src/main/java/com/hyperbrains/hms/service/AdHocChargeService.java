package com.hyperbrains.hms.service;

import com.hyperbrains.hms.service.dto.AdHocChargeDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.hyperbrains.hms.domain.AdHocCharge}.
 */
public interface AdHocChargeService {
    /**
     * Save a adHocCharge.
     *
     * @param adHocChargeDTO the entity to save.
     * @return the persisted entity.
     */
    AdHocChargeDTO save(AdHocChargeDTO adHocChargeDTO);

    /**
     * Updates a adHocCharge.
     *
     * @param adHocChargeDTO the entity to update.
     * @return the persisted entity.
     */
    AdHocChargeDTO update(AdHocChargeDTO adHocChargeDTO);

    /**
     * Partially updates a adHocCharge.
     *
     * @param adHocChargeDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<AdHocChargeDTO> partialUpdate(AdHocChargeDTO adHocChargeDTO);

    /**
     * Get all the adHocCharges.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<AdHocChargeDTO> findAll(Pageable pageable);

    /**
     * Get all the adHocCharges with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<AdHocChargeDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" adHocCharge.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<AdHocChargeDTO> findOne(Long id);

    /**
     * Delete the "id" adHocCharge.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
