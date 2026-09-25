package com.hyperbrains.hms.repository;

import com.hyperbrains.hms.domain.DiagnosticOrder;
import com.hyperbrains.hms.domain.enumeration.OrderStatus;
import com.hyperbrains.hms.domain.enumeration.OrderType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the DiagnosticOrder entity.
 */
@Repository
public interface DiagnosticOrderRepository extends JpaRepository<DiagnosticOrder, Long> {
    @Query("select diagnosticOrder from DiagnosticOrder diagnosticOrder where diagnosticOrder.orderedBy.login = ?#{authentication.name}")
    List<DiagnosticOrder> findByOrderedByIsCurrentUser();

    /**
     * How much diagnostic work is still outstanding for a visit.
     *
     * <p>A count in the database rather than a loaded collection: this runs on every order
     * resolution and on every consultation completion, and only the number is needed.
     */
    @Query(
        """
        select count(o) from DiagnosticOrder o
        where o.visit.id = :visitId
          and o.status not in (
            com.hyperbrains.hms.domain.enumeration.OrderStatus.COMPLETED,
            com.hyperbrains.hms.domain.enumeration.OrderStatus.CANCELLED
          )
        """
    )
    long countOutstandingByVisitId(@Param("visitId") Long visitId);

    /**
     * A lab or radiology worklist.
     *
     * <p>The type filter is a <em>query</em> parameter, not a post-filter on the response. A Lab
     * user must never be able to see a radiology order at all, which cannot be achieved by fetching
     * everything and hiding part of it — the rows would still have left the database.
     *
     * <p>Patient and visit are joined because every worklist row shows whose specimen or image this
     * is.
     */
    @Query(
        value = """
            select o from DiagnosticOrder o
            left join fetch o.visit v
            left join fetch v.patient
            where o.type in :types
              and o.status in :statuses
            order by o.orderedAt asc
            """,
        countQuery = """
            select count(o) from DiagnosticOrder o
            where o.type in :types
              and o.status in :statuses
            """
    )
    Page<DiagnosticOrder> findWorklist(
        @Param("types") Collection<OrderType> types,
        @Param("statuses") Collection<OrderStatus> statuses,
        Pageable pageable
    );

    /** Everything ordered during a visit, oldest first. */
    List<DiagnosticOrder> findByVisitIdOrderByOrderedAtAsc(Long visitId);

    default Optional<DiagnosticOrder> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<DiagnosticOrder> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<DiagnosticOrder> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select diagnosticOrder from DiagnosticOrder diagnosticOrder left join fetch diagnosticOrder.orderedBy",
        countQuery = "select count(diagnosticOrder) from DiagnosticOrder diagnosticOrder"
    )
    Page<DiagnosticOrder> findAllWithToOneRelationships(Pageable pageable);

    @Query("select diagnosticOrder from DiagnosticOrder diagnosticOrder left join fetch diagnosticOrder.orderedBy")
    List<DiagnosticOrder> findAllWithToOneRelationships();

    @Query(
        "select diagnosticOrder from DiagnosticOrder diagnosticOrder left join fetch diagnosticOrder.orderedBy where diagnosticOrder.id =:id"
    )
    Optional<DiagnosticOrder> findOneWithToOneRelationships(@Param("id") Long id);
}
