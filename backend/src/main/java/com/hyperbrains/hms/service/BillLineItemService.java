package com.hyperbrains.hms.service;

import com.hyperbrains.hms.service.dto.BillLineItemDTO;
import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link com.hyperbrains.hms.domain.BillLineItem}.
 */
public interface BillLineItemService {
    /**
     * Save a billLineItem.
     *
     * @param billLineItemDTO the entity to save.
     * @return the persisted entity.
     */
    BillLineItemDTO save(BillLineItemDTO billLineItemDTO);

    /**
     * Updates a billLineItem.
     *
     * @param billLineItemDTO the entity to update.
     * @return the persisted entity.
     */
    BillLineItemDTO update(BillLineItemDTO billLineItemDTO);

    /**
     * Partially updates a billLineItem.
     *
     * @param billLineItemDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<BillLineItemDTO> partialUpdate(BillLineItemDTO billLineItemDTO);

    /**
     * Get all the billLineItems.
     *
     * @return the list of entities.
     */
    List<BillLineItemDTO> findAll();

    /**
     * Get the "id" billLineItem.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<BillLineItemDTO> findOne(Long id);

    /**
     * Delete the "id" billLineItem.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
