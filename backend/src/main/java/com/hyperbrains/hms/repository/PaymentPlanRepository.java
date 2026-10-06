package com.hyperbrains.hms.repository;

import com.hyperbrains.hms.domain.PaymentPlan;
import com.hyperbrains.hms.domain.enumeration.PaymentPlanStatus;
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

    /**
     * Whether a bill is covered by an agreement to pay, which is one of the three ways a discharge may proceed.
     *
     * <p>A counted existence rather than a row: the discharge gate reads the answer, not the terms. If the plan's
     * instalments are ever needed — to say how much is still owed — that is a different question and a different
     * query, and the gate should not start carrying terms it does not use.
     */
    boolean existsByBillIdAndStatus(Long billId, PaymentPlanStatus status);

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
