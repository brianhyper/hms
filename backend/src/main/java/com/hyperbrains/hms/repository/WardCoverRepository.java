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
}
