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
    /**
     * This stay's chart, oldest first.
     *
     * <p>The chart is read chronologically — that is what makes it a log of a stay rather than a table of
     * figures — so the order is the query's job, not the reader's. Ordered by time and then by id, because
     * two observations charted in the same instant must not come back in an order the database chose, and a
     * chart that shows a correction before the reading it corrects is worse than no chart.
     *
     * <p>{@code recordedBy} is fetched because the reader needs the name of whoever charted each row, and
     * {@code corrects} so the supersession link resolves without a second query per row.
     */
    @Query(
        """
        select vitals from InpatientVitals vitals
        join fetch vitals.recordedBy
        left join fetch vitals.corrects
        where vitals.admission.id = :admissionId
        order by vitals.recordedAt asc, vitals.id asc
        """
    )
    List<InpatientVitals> findChart(@Param("admissionId") Long admissionId);

    /**
     * Whether some later observation already replaces this one.
     *
     * <p>Checked before a correction is accepted, so that a row can never be replaced twice: two rows each
     * claiming to supersede the same reading leave the chart with no answer to "which figure is in force".
     */
    boolean existsByCorrectsId(Long correctsId);

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
