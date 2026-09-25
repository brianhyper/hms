package com.hyperbrains.hms.service;

import com.hyperbrains.hms.service.dto.ConsultationDTO;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.hyperbrains.hms.domain.Consultation}.
 */
public interface ConsultationService {
    /**
     * Save a consultation.
     *
     * @param consultationDTO the entity to save.
     * @return the persisted entity.
     */
    ConsultationDTO save(ConsultationDTO consultationDTO);

    /**
     * Updates a consultation.
     *
     * @param consultationDTO the entity to update.
     * @return the persisted entity.
     */
    ConsultationDTO update(ConsultationDTO consultationDTO);

    /**
     * Partially updates a consultation.
     *
     * @param consultationDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<ConsultationDTO> partialUpdate(ConsultationDTO consultationDTO);

    /**
     * Get all the consultations.
     *
     * @return the list of entities.
     */
    List<ConsultationDTO> findAll();

    /**
     * Get all the ConsultationDTO where Visit is {@code null}.
     *
     * @return the {@link List} of entities.
     */
    List<ConsultationDTO> findAllWhereVisitIsNull();

    /**
     * Get all the consultations with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<ConsultationDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" consultation.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<ConsultationDTO> findOne(Long id);

    /**
     * Delete the "id" consultation.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
