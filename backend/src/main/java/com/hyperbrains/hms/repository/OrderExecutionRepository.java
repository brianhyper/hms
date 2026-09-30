package com.hyperbrains.hms.repository;

import com.hyperbrains.hms.domain.OrderExecution;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the OrderExecution entity.
 */
@Repository
public interface OrderExecutionRepository extends JpaRepository<OrderExecution, Long> {
    /** Every occasion an order was carried out, oldest first, with who did it. */
    @Query(
        "select execution from OrderExecution execution join fetch execution.executedBy where execution.doctorOrder.id = :orderId order by execution.executedAt asc, execution.id asc"
    )
    List<OrderExecution> findForOrder(@Param("orderId") Long orderId);

    /**
     * The same for a whole sheet at once.
     *
     * <p>One query rather than one per order: an order sheet is read on every ward round, and a sheet with
     * twenty orders would otherwise be twenty round trips for the same screen.
     */
    @Query(
        "select execution from OrderExecution execution join fetch execution.executedBy where execution.doctorOrder.id in :orderIds order by execution.executedAt asc, execution.id asc"
    )
    List<OrderExecution> findForOrders(@Param("orderIds") java.util.Collection<Long> orderIds);

    @Query("select orderExecution from OrderExecution orderExecution where orderExecution.executedBy.login = ?#{authentication.name}")
    List<OrderExecution> findByExecutedByIsCurrentUser();

    default Optional<OrderExecution> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<OrderExecution> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<OrderExecution> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select orderExecution from OrderExecution orderExecution left join fetch orderExecution.executedBy",
        countQuery = "select count(orderExecution) from OrderExecution orderExecution"
    )
    Page<OrderExecution> findAllWithToOneRelationships(Pageable pageable);

    @Query("select orderExecution from OrderExecution orderExecution left join fetch orderExecution.executedBy")
    List<OrderExecution> findAllWithToOneRelationships();

    @Query(
        "select orderExecution from OrderExecution orderExecution left join fetch orderExecution.executedBy where orderExecution.id =:id"
    )
    Optional<OrderExecution> findOneWithToOneRelationships(@Param("id") Long id);
}
