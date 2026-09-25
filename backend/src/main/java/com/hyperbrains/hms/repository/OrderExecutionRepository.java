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
