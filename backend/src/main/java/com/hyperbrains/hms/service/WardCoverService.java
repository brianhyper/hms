package com.hyperbrains.hms.service;

import com.hyperbrains.hms.service.dto.WardCoverDTO;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.hyperbrains.hms.domain.WardCover}.
 */
public interface WardCoverService {
    /**
     * Save a wardCover.
     *
     * @param wardCoverDTO the entity to save.
     * @return the persisted entity.
     */
    WardCoverDTO save(WardCoverDTO wardCoverDTO);

    /**
     * Updates a wardCover.
     *
     * @param wardCoverDTO the entity to update.
     * @return the persisted entity.
     */
    WardCoverDTO update(WardCoverDTO wardCoverDTO);

    /**
     * Partially updates a wardCover.
     *
     * @param wardCoverDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<WardCoverDTO> partialUpdate(WardCoverDTO wardCoverDTO);

    /**
     * Get all the wardCovers.
     *
     * @return the list of entities.
     */
    List<WardCoverDTO> findAll();

    /**
     * Get all the wardCovers with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<WardCoverDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" wardCover.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<WardCoverDTO> findOne(Long id);

    /**
     * Delete the "id" wardCover.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
