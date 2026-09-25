package com.hyperbrains.hms.service;

import com.hyperbrains.hms.service.dto.DoctorOrderDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.hyperbrains.hms.domain.DoctorOrder}.
 */
public interface DoctorOrderService {
    /**
     * Save a doctorOrder.
     *
     * @param doctorOrderDTO the entity to save.
     * @return the persisted entity.
     */
    DoctorOrderDTO save(DoctorOrderDTO doctorOrderDTO);

    /**
     * Updates a doctorOrder.
     *
     * @param doctorOrderDTO the entity to update.
     * @return the persisted entity.
     */
    DoctorOrderDTO update(DoctorOrderDTO doctorOrderDTO);

    /**
     * Partially updates a doctorOrder.
     *
     * @param doctorOrderDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<DoctorOrderDTO> partialUpdate(DoctorOrderDTO doctorOrderDTO);

    /**
     * Get all the doctorOrders.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<DoctorOrderDTO> findAll(Pageable pageable);

    /**
     * Get all the doctorOrders with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<DoctorOrderDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" doctorOrder.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<DoctorOrderDTO> findOne(Long id);

    /**
     * Delete the "id" doctorOrder.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
