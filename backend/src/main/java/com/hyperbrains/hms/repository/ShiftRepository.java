package com.hyperbrains.hms.repository;

import com.hyperbrains.hms.domain.Shift;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Shift entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ShiftRepository extends JpaRepository<Shift, Long> {
    /**
     * The shifts written for a set of dates, with the person, their department and the ward already read.
     *
     * <p>Returns candidates rather than an answer: which of them is on duty at a given moment is decided by
     * {@code ShiftDuty}, in one place, rather than by a window comparison restated here in JPQL where the unit tests
     * cannot reach it. The dates asked for are the ones the rule names — today and the day before, so a night shift
     * is found in the small hours of the day after it started.
     *
     * <p>Everything the ward-facing view touches is fetched here. A view that lazily loaded its own way out of a
     * stream would work in a test and fail on a full roster.
     */
    @Query(
        "select shift from Shift shift join fetch shift.staffRecord staff join fetch staff.department left join fetch shift.ward where shift.shiftDate in :dates order by shift.startsAt asc, staff.fullName asc"
    )
    List<Shift> findWithPeopleOnDates(@Param("dates") Collection<LocalDate> dates);
}
