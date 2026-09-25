package com.hyperbrains.hms.service;

import com.hyperbrains.hms.service.dto.ReferralDTO;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.hyperbrains.hms.domain.Referral}.
 */
public interface ReferralService {
    /**
     * Save a referral.
     *
     * @param referralDTO the entity to save.
     * @return the persisted entity.
     */
    ReferralDTO save(ReferralDTO referralDTO);

    /**
     * Updates a referral.
     *
     * @param referralDTO the entity to update.
     * @return the persisted entity.
     */
    ReferralDTO update(ReferralDTO referralDTO);

    /**
     * Partially updates a referral.
     *
     * @param referralDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<ReferralDTO> partialUpdate(ReferralDTO referralDTO);

    /**
     * Get all the referrals.
     *
     * @return the list of entities.
     */
    List<ReferralDTO> findAll();

    /**
     * Get all the referrals with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<ReferralDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" referral.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<ReferralDTO> findOne(Long id);

    /**
     * Delete the "id" referral.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
