package com.hyperbrains.hms.repository;

import com.hyperbrains.hms.domain.Payment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Payment entity.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    @Query("select payment from Payment payment where payment.recordedBy.login = ?#{authentication.name}")
    List<Payment> findByRecordedByIsCurrentUser();

    default Optional<Payment> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Payment> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Payment> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select payment from Payment payment left join fetch payment.recordedBy",
        countQuery = "select count(payment) from Payment payment"
    )
    Page<Payment> findAllWithToOneRelationships(Pageable pageable);

    @Query("select payment from Payment payment left join fetch payment.recordedBy")
    List<Payment> findAllWithToOneRelationships();

    @Query("select payment from Payment payment left join fetch payment.recordedBy where payment.id =:id")
    Optional<Payment> findOneWithToOneRelationships(@Param("id") Long id);

    /**
     * The payment a receipt number already belongs to.
     *
     * <p>Receipt numbers are unique, so a retry with the same receipt must be recognised and answered
     * rather than allowed to reach the database and come back as a constraint violation.
     */
    Optional<Payment> findOneByReceiptNumber(String receiptNumber);
}