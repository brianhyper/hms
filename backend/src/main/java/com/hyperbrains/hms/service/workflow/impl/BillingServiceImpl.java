package com.hyperbrains.hms.service.workflow.impl;

import com.hyperbrains.hms.domain.Bill;
import com.hyperbrains.hms.domain.BillLineItem;
import com.hyperbrains.hms.domain.Visit;
import com.hyperbrains.hms.domain.enumeration.BillLineSourceType;
import com.hyperbrains.hms.domain.enumeration.BillStatus;
import com.hyperbrains.hms.repository.BillLineItemRepository;
import com.hyperbrains.hms.repository.BillRepository;
import com.hyperbrains.hms.repository.VisitRepository;
import com.hyperbrains.hms.service.AuditActions;
import com.hyperbrains.hms.service.AuditLogService;
import com.hyperbrains.hms.service.workflow.BillingService;
import java.math.BigDecimal;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BillingServiceImpl implements BillingService {

    private static final Logger LOG = LoggerFactory.getLogger(BillingServiceImpl.class);

    private final BillRepository billRepository;

    private final BillLineItemRepository billLineItemRepository;

    private final VisitRepository visitRepository;

    private final AuditLogService auditLogService;

    public BillingServiceImpl(
        BillRepository billRepository,
        BillLineItemRepository billLineItemRepository,
        VisitRepository visitRepository,
        AuditLogService auditLogService
    ) {
        this.billRepository = billRepository;
        this.billLineItemRepository = billLineItemRepository;
        this.visitRepository = visitRepository;
        this.auditLogService = auditLogService;
    }

    @Override
    public Bill ensureBill(Visit visit) {
        Bill existing = visit.getBill();
        if (existing != null) {
            return existing;
        }

        Bill bill = new Bill();
        bill.setTotalAmount(BigDecimal.ZERO);
        bill.setStatus(BillStatus.UNPAID);
        bill = billRepository.save(bill);

        visit.setBill(bill);
        visitRepository.save(visit);

        LOG.debug("Opened bill {} for visit {}", bill.getId(), visit.getId());
        return bill;
    }

    @Override
    public BillLineItem addOrUpdateLine(
        Bill bill,
        BillLineSourceType sourceType,
        String sourceRef,
        String description,
        BigDecimal amount
    ) {
        BillLineItem line = billLineItemRepository.findOneByBillIdAndSourceRef(bill.getId(), sourceRef).orElseGet(BillLineItem::new);
        boolean isNew = line.getId() == null;

        line.setBill(bill);
        line.setSourceType(sourceType);
        line.setSourceRef(sourceRef);
        line.setDescription(description);
        line.setAmount(amount);
        line = billLineItemRepository.save(line);

        // Recorded even for an update: "this charge was restated" is exactly the kind of thing an
        // auditor asks about later.
        auditLogService.record(
            AuditLogService.Entry.of(AuditActions.BILL_LINE_ADDED, "BillLineItem", line.getId()).withDetails(
                (isNew ? "Added " : "Restated ") + description + " = " + amount + " (" + sourceType + ") on bill " + bill.getId()
            )
        );

        return line;
    }

    @Override
    public boolean removeLine(Bill bill, BillLineSourceType sourceType, Long sourceId) {
        String ref = BillingService.sourceRef(sourceType, sourceId);
        Optional<BillLineItem> existing = billLineItemRepository.findOneByBillIdAndSourceRef(bill.getId(), ref);
        if (existing.isEmpty()) {
            // Nothing was ever charged for this source, or a retry is asking twice. Not an error.
            return false;
        }

        BillLineItem line = existing.orElseThrow();
        billLineItemRepository.delete(line);

        // Voided rather than silently deleted: a charge that existed and then did not is precisely
        // what an auditor asks about later.
        auditLogService.record(
            AuditLogService.Entry.of(AuditActions.BILL_LINE_VOIDED, "BillLineItem", line.getId()).withDetails(
                "Voided " + line.getDescription() + " = " + line.getAmount() + " (" + sourceType + ") on bill " + bill.getId()
            )
        );

        LOG.debug("Voided charge {} on bill {}", ref, bill.getId());
        return true;
    }

    @Override
    public Bill recalculateTotal(Bill bill) {
        BigDecimal total = billLineItemRepository.sumAmountsByBillId(bill.getId());
        bill.setTotalAmount(total == null ? BigDecimal.ZERO : total);
        Bill saved = billRepository.save(bill);
        LOG.debug("Bill {} total is now {}", saved.getId(), saved.getTotalAmount());
        return saved;
    }
}
