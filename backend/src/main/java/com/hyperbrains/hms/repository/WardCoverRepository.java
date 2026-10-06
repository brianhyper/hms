package com.hyperbrains.hms.repository;

import com.hyperbrains.hms.domain.WardCover;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the WardCover entity.
 */
@Repository
public interface WardCoverRepository extends JpaRepository<WardCover, Long> {
    /**
     * This doctor's roster, with the ward and both users already read.
     *
     * <p>Used to answer "which wards is this doctor covering right now", so it deliberately returns the
     * periods rather than a filtered set: the window comparison is done by {@code WardCoverage}, in one
     * place, rather than here in JPQL where it would be a second copy of the same rule.
     */
    @Query(
        "select cover from WardCover cover join fetch cover.doctor join fetch cover.ward join fetch cover.assignedBy where cover.doctor.id = :doctorId order by cover.coversFrom desc, cover.id desc"
    )
    List<WardCover> findByDoctorIdWithWard(@Param("doctorId") Long doctorId);

    /** The whole roster, ward by ward, for the screen that manages it. */
    @Query(
        "select cover from WardCover cover join fetch cover.doctor join fetch cover.ward join fetch cover.assignedBy order by cover.ward.name asc, cover.coversFrom desc, cover.id desc"
    )
    List<WardCover> findAllWithWard();

    @Query("select wardCover from WardCover wardCover where wardCover.doctor.login = ?#{authentication.name}")
    List<WardCover> findByDoctorIsCurrentUser();

    @Query("select wardCover from WardCover wardCover where wardCover.assignedBy.login = ?#{authentication.name}")
    List<WardCover> findByAssignedByIsCurrentUser();

    default Optional<WardCover> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<WardCover> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<WardCover> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select wardCover from WardCover wardCover left join fetch wardCover.doctor left join fetch wardCover.assignedBy",
        countQuery = "select count(wardCover) from WardCover wardCover"
    )
    Page<WardCover> findAllWithToOneRelationships(Pageable pageable);

    @Query("select wardCover from WardCover wardCover left join fetch wardCover.doctor left join fetch wardCover.assignedBy")
    List<WardCover> findAllWithToOneRelationships();

    @Query(
        "select wardCover from WardCover wardCover left join fetch wardCover.doctor left join fetch wardCover.assignedBy where wardCover.id =:id"
    )
    Optional<WardCover> findOneWithToOneRelationships(@Param("id") Long id);

    /**
     * The identifiers of the ward and the two people on a cover, as plain values.
     *
     * <p>A scalar projection, not the cover's own references: reading an id off a Hibernate-backed reference forces a
     * load and can throw, which is what stopped an id-based reference guard being added the first time. The service's
     * reference guard reads both sides from here and from the request, never through a proxy.
     */
    interface ReferenceIds {
        Long getDoctorId();

        Long getWardId();

        Long getAssignedById();
    }

    @Query(
        "select cover.doctor.id as doctorId, cover.ward.id as wardId, cover.assignedBy.id as assignedById from WardCover cover where cover.id = :id"
    )
    Optional<ReferenceIds> findReferenceIds(@Param("id") Long id);
}
