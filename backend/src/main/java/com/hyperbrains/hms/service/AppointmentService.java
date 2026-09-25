package com.hyperbrains.hms.service;

import com.hyperbrains.hms.service.dto.AppointmentDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.hyperbrains.hms.domain.Appointment}.
 */
public interface AppointmentService {
    /**
     * Save a appointment.
     *
     * @param appointmentDTO the entity to save.
     * @return the persisted entity.
     */
    AppointmentDTO save(AppointmentDTO appointmentDTO);

    /**
     * Updates a appointment.
     *
     * @param appointmentDTO the entity to update.
     * @return the persisted entity.
     */
    AppointmentDTO update(AppointmentDTO appointmentDTO);

    /**
     * Partially updates a appointment.
     *
     * @param appointmentDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<AppointmentDTO> partialUpdate(AppointmentDTO appointmentDTO);

    /**
     * Get all the appointments.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<AppointmentDTO> findAll(Pageable pageable);

    /**
     * Get all the appointments with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<AppointmentDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" appointment.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<AppointmentDTO> findOne(Long id);

    /**
     * Delete the "id" appointment.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
