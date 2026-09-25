package com.hyperbrains.hms.repository;

import com.hyperbrains.hms.domain.BillLineItem;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the BillLineItem entity.
 */
@SuppressWarnings("unused")
@Repository
public interface BillLineItemRepository extends JpaRepository<BillLineItem, Long> {
    /**
     * The line item a given source already produced, if any.
     *
     * <p>This is what makes incremental charging safe to retry. Every charge is keyed by what
     * produced it ({@code "CONSULTATION:42"}), so a repeated "complete consultation" finds the
     * existing line and updates it rather than charging the patient a second time.
     */
    Optional<BillLineItem> findOneByBillIdAndSourceRef(Long billId, String sourceRef);

    /** Summed in the database: the bill's total is a figure Finance acts on, so it is not assembled client-side. */
    @Query("select coalesce(sum(l.amount), 0) from BillLineItem l where l.bill.id = :billId")
    BigDecimal sumAmountsByBillId(@Param("billId") Long billId);

    /** Everything charged on a bill, in insertion order. */
    List<BillLineItem> findByBillIdOrderByIdAsc(Long billId);
}
