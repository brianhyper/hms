package com.hyperbrains.hms.service.workflow;

import com.hyperbrains.hms.domain.Bill;
import com.hyperbrains.hms.domain.BillLineItem;
import com.hyperbrains.hms.domain.Visit;
import com.hyperbrains.hms.domain.enumeration.BillLineSourceType;
import java.math.BigDecimal;

/**
 * Incremental billing.
 *
 * <p>Charges are not assembled in one pass at the end of a visit. Each thing that gets done adds
 * its own line item at the moment it resolves — a completed consultation, a released lab result, a
 * dispensed prescription — so the bill is always an accurate statement of what has actually been
 * delivered, and a partial visit still has a correct total.
 */
public interface BillingService {

    /**
     * The visit's bill, created empty on first use.
     *
     * <p>Created eagerly at the first charge rather than at registration, so a visit that never
     * generates a charge never has a bill. The column is nullable precisely to allow that.
     */
    Bill ensureBill(Visit visit);

    /**
     * Record a charge against whatever produced it, or update the existing one.
     *
     * <p>Keyed on {@code (bill, sourceRef)} so it is idempotent: retrying the action that produced
     * the charge updates the same line instead of billing the patient twice.
     */
    BillLineItem addOrUpdateLine(
        Bill bill,
        BillLineSourceType sourceType,
        String sourceRef,
        String description,
        BigDecimal amount
    );

    /**
     * Remove the charge a source produced, if it produced one.
     *
     * <p>The counterpart to {@link #addOrUpdateLine}, and needed for the same reason charges are
     * raised incrementally: something that is undone — a withdrawn prescription — must not leave a
     * charge behind for medicine that will never be handed over.
     *
     * <p>Idempotent: a source that produced no charge is not an error, because the caller may be
     * retrying.
     *
     * @return whether a line was actually removed
     */
    boolean removeLine(Bill bill, BillLineSourceType sourceType, Long sourceId);

    /**
     * Sum the line items into the stored total.
     *
     * <p>Only called when the visit reaches the payment stage, which is when the bill becomes a
     * figure Finance actually collects against.
     */
    Bill recalculateTotal(Bill bill);

    /**
     * The canonical source reference for a charge: {@code "<SOURCE_TYPE>:<id>"}.
     *
     * <p>Defined here so every caller formats it identically, since it is the uniqueness key the
     * idempotency depends on.
     */
    static String sourceRef(BillLineSourceType sourceType, Long id) {
        return sourceType.name() + ":" + id;
    }
}
