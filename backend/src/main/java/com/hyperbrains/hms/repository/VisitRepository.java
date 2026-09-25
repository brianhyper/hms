package com.hyperbrains.hms.repository;

import com.hyperbrains.hms.domain.Visit;
import com.hyperbrains.hms.domain.enumeration.VisitStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Visit entity.
 */
@SuppressWarnings("unused")
@Repository
public interface VisitRepository extends JpaRepository<Visit, Long> {
    /**
     * A live work queue, ordered the way staff expect to work through it: emergency first, then
     * urgent, then normal, and oldest first within a band.
     *
     * <p>The CASE expression reproduces {@code VisitLifecycle.queueWeight} deliberately. The
     * priority column holds a string, so {@code order by v.priority} would sort alphabetically —
     * EMERGENCY, NORMAL, URGENT — and quietly put urgent patients last. The numbers here and in
     * that class must change together.
     *
     * <p>{@code left join fetch} on the patient because every queue row shows the patient's name
     * and identifier; without it, rendering a page of twenty queue rows costs twenty extra queries
     * per poll. The explicit count query is needed because a fetch join cannot be counted directly.
     */
    @Query(
        value = """
            select v from Visit v
            left join fetch v.patient
            where v.status in :statuses
            order by
              case v.priority
                when com.hyperbrains.hms.domain.enumeration.VisitPriority.EMERGENCY then 0
                when com.hyperbrains.hms.domain.enumeration.VisitPriority.URGENT then 1
                else 2
              end asc,
              v.createdAt asc
            """,
        countQuery = "select count(v) from Visit v where v.status in :statuses"
    )
    Page<Visit> findQueue(@Param("statuses") Collection<VisitStatus> statuses, Pageable pageable);

    /**
     * Every visit belonging to a patient.
     *
     * <p>Exists for the merge workflow, which has to re-point a whole record's clinical history at
     * the surviving patient, and is used by tests to tear a patient down without leaving rows that
     * make the delete fail on a foreign key.
     */
    List<Visit> findByPatientId(Long patientId);

    /**
     * The visit a consultation belongs to.
     *
     * <p>Read from this side rather than through {@code Consultation.visit} because that association
     * is the inverse of this one and would need a lazy load; the owning column is
     * {@code visit.consultation_id}, so this is a direct lookup.
     */
    Optional<Visit> findOneByConsultationId(Long consultationId);
}
