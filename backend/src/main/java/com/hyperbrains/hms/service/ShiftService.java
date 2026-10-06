package com.hyperbrains.hms.service;

import com.hyperbrains.hms.service.dto.ShiftDTO;
import com.hyperbrains.hms.service.dto.view.ShiftViewDTO;
import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link com.hyperbrains.hms.domain.Shift}.
 *
 * <p>There is no delete, for the same reason a staff record has none: the roster is the record of who was on duty,
 * and removing a day from it rewrites what happened. A shift that was wrong is corrected; a shift that is no longer
 * needed is the roster's own operation to build, with its own rule about how far ahead that is allowed.
 */
public interface ShiftService {
    /**
     * Save a shift.
     *
     * @param shiftDTO the entity to save.
     * @return the persisted entity.
     */
    ShiftDTO save(ShiftDTO shiftDTO);

    /**
     * Updates a shift.
     *
     * @param shiftDTO the entity to update.
     * @return the persisted entity.
     */
    ShiftDTO update(ShiftDTO shiftDTO);

    /**
     * Partially updates a shift.
     *
     * @param shiftDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<ShiftDTO> partialUpdate(ShiftDTO shiftDTO);

    /**
     * Get all the shifts.
     *
     * @return the list of entities.
     */
    List<ShiftDTO> findAll();

    /**
     * Get the "id" shift.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<ShiftDTO> findOne(Long id);

    /**
     * Who is on duty right now, across the hospital.
     *
     * @return one entry per shift that covers this moment, named the way a ward may read it.
     */
    List<ShiftViewDTO> onDutyNow();

    /**
     * Who is on duty right now on one ward.
     *
     * @param wardId the ward to ask about.
     * @return the shifts covering this moment on that ward, empty when nobody is.
     */
    List<ShiftViewDTO> onDutyNowInWard(Long wardId);
}
