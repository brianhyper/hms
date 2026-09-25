package com.hyperbrains.hms.repository;

import com.hyperbrains.hms.domain.Dispense;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Dispense entity.
 */
@Repository
public interface DispenseRepository extends JpaRepository<Dispense, Long> {
    @Query("select dispense from Dispense dispense where dispense.recordedBy.login = ?#{authentication.name}")
    List<Dispense> findByRecordedByIsCurrentUser();

    default Optional<Dispense> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Dispense> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Dispense> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select dispense from Dispense dispense left join fetch dispense.recordedBy",
        countQuery = "select count(dispense) from Dispense dispense"
    )
    Page<Dispense> findAllWithToOneRelationships(Pageable pageable);

    @Query("select dispense from Dispense dispense left join fetch dispense.recordedBy")
    List<Dispense> findAllWithToOneRelationships();

    @Query("select dispense from Dispense dispense left join fetch dispense.recordedBy where dispense.id =:id")
    Optional<Dispense> findOneWithToOneRelationships(@Param("id") Long id);
}
