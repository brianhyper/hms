package com.hyperbrains.hms.service.workflow;

import com.hyperbrains.hms.service.dto.view.BillViewDTO;
import com.hyperbrains.hms.service.dto.view.RecordPaymentRequestDTO;

/**
 * Collecting the money.
 *
 * <p>Named {@code ...Workflow...} rather than {@code PaymentService} because the generated CRUD service
 * already owns that bean name.
 *
 * <p>Payment is where three otherwise separate guarantees meet, and this is the only place they are
 * allowed to meet: the bill becomes collectable only once the visit is at the payment stage, the bill
 * settles only when a confirmed payment covers it, and settling the bill is what releases medicine to
 * the pharmacy.
 */
public interface PaymentWorkflowService {

    /**
     * The bill as the cashier sees it.
     *
     * <p>A visit with no charges has no bill yet; that is answered as nothing owed rather than as an
     * error, because "this patient owes nothing" is a legitimate thing for a desk to ask.
     */
    BillViewDTO billForVisit(Long visitId);

    /**
     * Record money received, and settle the bill if this payment covers it.
     *
     * <p>Adds to whatever is already recorded, so a deposit followed by the balance works. Records the
     * payment either way: an underpayment is money the hospital is holding, and refusing to record it
     * would leave it invisible.
     *
     * <p>Settlement — and therefore releasing medicine to the pharmacy — requires the payment to be
     * {@code CONFIRMED}. A pending insurance claim is recorded but settles nothing.
     *
     * @throws com.hyperbrains.hms.service.BusinessRuleViolationException if the visit is not at the
     *         payment stage, the bill is already settled, the receipt number is already used, the
     *         payment is rejected, or the amount would overshoot what is owed
     */
    BillViewDTO recordPayment(Long visitId, RecordPaymentRequestDTO request);
}
