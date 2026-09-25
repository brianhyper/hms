package com.hyperbrains.hms.repository;

import com.hyperbrains.hms.domain.InpatientVitals;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the InpatientVitals entity.
 */
@Repository
public interface InpatientVitalsRepository extends JpaRepository<InpatientVitals, Long> {
    @Query("select inpatientVitals from InpatientVitals inpatientVitals where inpatientVitals.recordedBy.login = ?#{authentication.name}")
    List<InpatientVitals> findByRecordedByIsCurrentUser();

    default Optional<InpatientVitals> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<InpatientVitals> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<InpatientVitals> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select inpatientVitals from InpatientVitals inpatientVitals left join fetch inpatientVitals.recordedBy",
        countQuery = "select count(inpatientVitals) from InpatientVitals inpatientVitals"
    )
    Page<InpatientVitals> findAllWithToOneRelationships(Pageable pageable);

    @Query("select inpatientVitals from InpatientVitals inpatientVitals left join fetch inpatientVitals.recordedBy")
    List<InpatientVitals> findAllWithToOneRelationships();

    @Query(
        "select inpatientVitals from InpatientVitals inpatientVitals left join fetch inpatientVitals.recordedBy where inpatientVitals.id =:id"
    )
    Optional<InpatientVitals> findOneWithToOneRelationships(@Param("id") Long id);
}
