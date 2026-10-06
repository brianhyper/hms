package com.hyperbrains.hms.repository;

import com.hyperbrains.hms.domain.Bill;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Bill entity.
 */
@SuppressWarnings("unused")
@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {
    /**
     * The identifiers the reference guard compares against: the bill itself, and the payment that settled it.
     *
     * <p>A scalar projection, not the bill's own references: reading an id off a Hibernate-backed reference forces a
     * load and can throw, which is what stopped an id-based reference guard being added the first time. The service's
     * reference guard reads both sides from here and from the request, never through a proxy.
     *
     * <p>The bill's own id is part of the projection because the payment is optional. A bill that no payment has
     * settled projects to an all-null row, and an all-null row comes back as no result — which would make "nothing has
     * settled this bill" indistinguishable from "there is no such bill". Selecting a column that is never null keeps
     * those two apart, so this method throws for a missing bill and answers for an unsettled one.
     */
    @Query("select bill.id as id, pay.id as paymentId from Bill bill left join bill.payment pay where bill.id = :id")
    Optional<ReferenceIds> findReferenceIds(@Param("id") Long id);

    /** A bill's own identifier, and the payment it names, as plain values. */
    interface ReferenceIds {
        Long getId();

        Long getPaymentId();
    }
}
