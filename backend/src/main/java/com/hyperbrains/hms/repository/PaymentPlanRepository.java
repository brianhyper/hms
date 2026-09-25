package com.hyperbrains.hms.repository;

import com.hyperbrains.hms.domain.PaymentPlan;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the PaymentPlan entity.
 */
@Repository
public interface PaymentPlanRepository extends JpaRepository<PaymentPlan, Long> {
    @Query("select paymentPlan from PaymentPlan paymentPlan where paymentPlan.agreedBy.login = ?#{authentication.name}")
    List<PaymentPlan> findByAgreedByIsCurrentUser();

    default Optional<PaymentPlan> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<PaymentPlan> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<PaymentPlan> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select paymentPlan from PaymentPlan paymentPlan left join fetch paymentPlan.agreedBy",
        countQuery = "select count(paymentPlan) from PaymentPlan paymentPlan"
    )
    Page<PaymentPlan> findAllWithToOneRelationships(Pageable pageable);

    @Query("select paymentPlan from PaymentPlan paymentPlan left join fetch paymentPlan.agreedBy")
    List<PaymentPlan> findAllWithToOneRelationships();

    @Query("select paymentPlan from PaymentPlan paymentPlan left join fetch paymentPlan.agreedBy where paymentPlan.id =:id")
    Optional<PaymentPlan> findOneWithToOneRelationships(@Param("id") Long id);
}
