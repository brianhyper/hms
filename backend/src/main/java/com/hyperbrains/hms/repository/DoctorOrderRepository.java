package com.hyperbrains.hms.repository;

import com.hyperbrains.hms.domain.DoctorOrder;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the DoctorOrder entity.
 */
@Repository
public interface DoctorOrderRepository extends JpaRepository<DoctorOrder, Long> {
    @Query("select doctorOrder from DoctorOrder doctorOrder where doctorOrder.orderedBy.login = ?#{authentication.name}")
    List<DoctorOrder> findByOrderedByIsCurrentUser();

    @Query("select doctorOrder from DoctorOrder doctorOrder where doctorOrder.cancelledBy.login = ?#{authentication.name}")
    List<DoctorOrder> findByCancelledByIsCurrentUser();

    default Optional<DoctorOrder> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<DoctorOrder> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<DoctorOrder> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select doctorOrder from DoctorOrder doctorOrder left join fetch doctorOrder.orderedBy left join fetch doctorOrder.cancelledBy",
        countQuery = "select count(doctorOrder) from DoctorOrder doctorOrder"
    )
    Page<DoctorOrder> findAllWithToOneRelationships(Pageable pageable);

    @Query("select doctorOrder from DoctorOrder doctorOrder left join fetch doctorOrder.orderedBy left join fetch doctorOrder.cancelledBy")
    List<DoctorOrder> findAllWithToOneRelationships();

    @Query(
        "select doctorOrder from DoctorOrder doctorOrder left join fetch doctorOrder.orderedBy left join fetch doctorOrder.cancelledBy where doctorOrder.id =:id"
    )
    Optional<DoctorOrder> findOneWithToOneRelationships(@Param("id") Long id);
}
