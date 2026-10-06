package com.hyperbrains.hms.repository;

import com.hyperbrains.hms.domain.DoctorOrder;
import com.hyperbrains.hms.domain.enumeration.DoctorOrderStatus;
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
    /**
     * This stay's orders, newest first, with the people and the linked prescription already read.
     *
     * <p>Newest first because that is how a ward sheet is read: what was written this morning matters more than
     * what was written on the day of admission. Ordered by time and then by id so two orders written in the
     * same instant cannot come back in an order the database chose.
     */
    @Query(
        """
        select orders from DoctorOrder orders
        join fetch orders.orderedBy
        left join fetch orders.cancelledBy
        left join fetch orders.prescription
        where orders.admission.id = :admissionId
        order by orders.orderedAt desc, orders.id desc
        """
    )
    List<DoctorOrder> findSheet(@Param("admissionId") Long admissionId);
    @Query("select doctorOrder from DoctorOrder doctorOrder where doctorOrder.orderedBy.login = ?#{authentication.name}")
    List<DoctorOrder> findByOrderedByIsCurrentUser();

    /**
     * This stay's orders in one status — used at discharge, where the running ones have to be surfaced.
     *
     * <p>Not {@code findSheet}, which is the medication sheet the ward reads: that answers "what is this patient
     * on", and the discharge question is "what is still running", which is a different shape of answer.
     */
    List<DoctorOrder> findByAdmissionIdAndStatus(Long admissionId, DoctorOrderStatus status);

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
