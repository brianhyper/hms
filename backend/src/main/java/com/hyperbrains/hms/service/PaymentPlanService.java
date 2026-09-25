package com.hyperbrains.hms.service;

import com.hyperbrains.hms.service.dto.PaymentPlanDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.hyperbrains.hms.domain.PaymentPlan}.
 */
public interface PaymentPlanService {
    /**
     * Save a paymentPlan.
     *
     * @param paymentPlanDTO the entity to save.
     * @return the persisted entity.
     */
    PaymentPlanDTO save(PaymentPlanDTO paymentPlanDTO);

    /**
     * Updates a paymentPlan.
     *
     * @param paymentPlanDTO the entity to update.
     * @return the persisted entity.
     */
    PaymentPlanDTO update(PaymentPlanDTO paymentPlanDTO);

    /**
     * Partially updates a paymentPlan.
     *
     * @param paymentPlanDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<PaymentPlanDTO> partialUpdate(PaymentPlanDTO paymentPlanDTO);

    /**
     * Get all the paymentPlans.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<PaymentPlanDTO> findAll(Pageable pageable);

    /**
     * Get all the paymentPlans with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<PaymentPlanDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" paymentPlan.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<PaymentPlanDTO> findOne(Long id);

    /**
     * Delete the "id" paymentPlan.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
