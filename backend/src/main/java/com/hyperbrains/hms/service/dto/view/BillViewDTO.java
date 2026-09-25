package com.hyperbrains.hms.service.dto.view;

import com.hyperbrains.hms.domain.Bill;
import com.hyperbrains.hms.domain.BillLineItem;
import com.hyperbrains.hms.domain.Payment;
import com.hyperbrains.hms.domain.enumeration.BillStatus;
import com.hyperbrains.hms.domain.enumeration.BillLineSourceType;
import com.hyperbrains.hms.domain.enumeration.PaymentConfirmationStatus;
import com.hyperbrains.hms.domain.enumeration.PaymentMethod;
import com.hyperbrains.hms.service.rules.BillSettlement;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * A visit's bill as the cashier sees it.
 *
 * <p>Carries the three figures that matter at a counter — what the bill is, what has been collected,
 * and what is still owed — because {@code outstanding} is exactly the number that gets asked out loud.
 * Deriving it client-side from the other two invites disagreement with the server.
 *
 * <p>The line items are described by what they are and what they cost. They deliberately carry no
 * clinical content: a consultation line says "General Consultation", not why the patient came.
 */
public class BillViewDTO implements Serializable {

    private Long billId;

    private Long visitId;

    private BillStatus status;

    private BigDecimal totalAmount;

    /** Everything recorded against this bill so far, which may be less than the total. */
    private BigDecimal recordedAmount;

    private BigDecimal outstanding;

    private Instant paidAt;

    private PaymentSummaryDTO payment;

    private List<BillLineItemViewDTO> lines;

    /**
     * A visit that has been charged nothing.
     *
     * <p>{@code billId} stays null because no bill exists yet — a bill is created at the first charge,
     * so a visit that never generates one never has one. Reporting zero owed rather than an error lets
     * a desk ask a legitimate question ("what does this patient owe?") without a failure path.
     */
    public static BillViewDTO empty(Long visitId) {
        BillViewDTO dto = new BillViewDTO();
        dto.visitId = visitId;
        dto.status = BillStatus.UNPAID;
        dto.totalAmount = BigDecimal.ZERO;
        dto.recordedAmount = BigDecimal.ZERO;
        dto.outstanding = BigDecimal.ZERO;
        dto.lines = List.of();
        return dto;
    }

    public static BillViewDTO from(Bill bill, Long visitId, List<BillLineItem> lines, Payment payment) {        BillViewDTO dto = new BillViewDTO();
        dto.billId = bill.getId();
        dto.visitId = visitId;
        dto.status = bill.getStatus();
        dto.totalAmount = bill.getTotalAmount();
        dto.paidAt = bill.getPaidAt();

        // A settled bill has been fully collected by definition, so anything still recorded against it
        // is the total rather than a figure that might drift.
        BigDecimal recorded = bill.getStatus() == BillStatus.PAID
            ? bill.getTotalAmount()
            : (payment == null ? BigDecimal.ZERO : payment.getAmount());
        dto.recordedAmount = recorded;
        dto.outstanding = BillSettlement.outstanding(bill.getTotalAmount(), recorded);

        dto.payment = payment == null ? null : PaymentSummaryDTO.from(payment);
        dto.lines = lines == null
            ? List.of()
            : lines
                  .stream()
                  .map(line -> new BillLineItemViewDTO(line.getId(), line.getSourceType(), line.getDescription(), line.getAmount()))
                  .toList();
        return dto;
    }

    /** One charge on the bill: what it was for and what it costs. */
    public record BillLineItemViewDTO(Long id, BillLineSourceType sourceType, String description, BigDecimal amount) {}

    /** The payment recorded against the bill. */
    public record PaymentSummaryDTO(
        Long id,
        PaymentMethod method,
        BigDecimal amount,
        String receiptNumber,
        String mpesaReference,
        String insurerName,
        PaymentConfirmationStatus confirmationStatus,
        Instant recordedAt,
        String recordedByLogin
    ) {
        static PaymentSummaryDTO from(Payment payment) {
            return new PaymentSummaryDTO(
                payment.getId(),
                payment.getMethod(),
                payment.getAmount(),
                payment.getReceiptNumber(),
                payment.getMpesaReference(),
                payment.getInsurerName(),
                payment.getConfirmationStatus(),
                payment.getRecordedAt(),
                payment.getRecordedBy() == null ? null : payment.getRecordedBy().getLogin()
            );
        }
    }

    public Long getBillId() {
        return billId;
    }

    public void setBillId(Long billId) {
        this.billId = billId;
    }

    public Long getVisitId() {
        return visitId;
    }

    public void setVisitId(Long visitId) {
        this.visitId = visitId;
    }

    public BillStatus getStatus() {
        return status;
    }

    public void setStatus(BillStatus status) {
        this.status = status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getRecordedAmount() {
        return recordedAmount;
    }

    public void setRecordedAmount(BigDecimal recordedAmount) {
        this.recordedAmount = recordedAmount;
    }

    public BigDecimal getOutstanding() {
        return outstanding;
    }

    public void setOutstanding(BigDecimal outstanding) {
        this.outstanding = outstanding;
    }

    public Instant getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(Instant paidAt) {
        this.paidAt = paidAt;
    }

    public PaymentSummaryDTO getPayment() {
        return payment;
    }

    public void setPayment(PaymentSummaryDTO payment) {
        this.payment = payment;
    }

    public List<BillLineItemViewDTO> getLines() {
        return lines;
    }

    public void setLines(List<BillLineItemViewDTO> lines) {
        this.lines = lines;
    }
}
