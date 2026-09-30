package com.hyperbrains.hms.service;

import com.hyperbrains.hms.service.dto.StaffRecordDTO;
import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link com.hyperbrains.hms.domain.StaffRecord}.
 *
 * <p>There is no delete: a member of staff who has left is recorded as {@code TERMINATED}. The record is a person's
 * employment history and the parent of the rostering, leave and payroll rows that follow, so removing it would
 * leave those rows pointing at nothing.
 */
public interface StaffRecordService {
    /**
     * Save a staff record.
     *
     * @param staffRecordDTO the entity to save.
     * @return the persisted entity.
     */
    StaffRecordDTO save(StaffRecordDTO staffRecordDTO);

    /**
     * Updates a staff record.
     *
     * @param staffRecordDTO the entity to update.
     * @return the persisted entity.
     */
    StaffRecordDTO update(StaffRecordDTO staffRecordDTO);

    /**
     * Partially updates a staff record.
     *
     * @param staffRecordDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<StaffRecordDTO> partialUpdate(StaffRecordDTO staffRecordDTO);

    /**
     * Get all the staff records.
     *
     * @return the list of entities.
     */
    List<StaffRecordDTO> findAll();

    /**
     * Get the "id" staff record.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<StaffRecordDTO> findOne(Long id);
}
