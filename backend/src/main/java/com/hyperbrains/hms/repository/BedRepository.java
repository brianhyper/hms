package com.hyperbrains.hms.repository;

import com.hyperbrains.hms.domain.Bed;
import com.hyperbrains.hms.domain.enumeration.BedStatus;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Bed entity.
 */
@SuppressWarnings("unused")
@Repository
public interface BedRepository extends JpaRepository<Bed, Long> {
    /**
     * Beds that can be given to a patient, optionally narrowed to one ward, one bed type, or both.
     *
     * <p>{@code ward.active} is part of the rule rather than a nicety. A closed ward's beds are physically
     * still there and still carry the status {@code AVAILABLE}, so the ward is the only thing that stops one
     * being handed out.
     *
     * <p>Ordered by ward and then bed number, so the list reads in the order somebody walking the corridor
     * would see the beds.
     *
     * <p>Null filters are passed as null rather than branching into four queries; Hibernate binds them with
     * the declared type, so PostgreSQL can resolve them.
     */
    @Query(
        """
        select b from Bed b
        join fetch b.ward w
        join fetch b.bedType
        where b.status = :status
          and w.active = true
          and (:wardId is null or w.id = :wardId)
          and (:bedTypeId is null or b.bedType.id = :bedTypeId)
        order by w.name asc, b.bedNumber asc
        """
    )
    List<Bed> findAssignable(@Param("status") BedStatus status, @Param("wardId") Long wardId, @Param("bedTypeId") Long bedTypeId);

    /**
     * Every bed, with its ward, that ward's department and its bed type already loaded.
     *
     * <p>Occupancy is counted in memory from this rather than by a group-by: the alternatives are one query
     * per status per ward, or a projection type to hold five counts, and a hospital has few enough beds that
     * the plainer code is worth the rows.
     */
    @Query("select b from Bed b join fetch b.ward w join fetch w.department join fetch b.bedType")
    List<Bed> findAllWithWardAndBedType();
}
