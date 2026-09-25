package com.hyperbrains.hms.service.impl;

import com.hyperbrains.hms.domain.Drug;
import com.hyperbrains.hms.repository.DrugRepository;
import com.hyperbrains.hms.service.AuditActions;
import com.hyperbrains.hms.service.AuditLogService;
import com.hyperbrains.hms.service.BusinessRuleViolationException;
import com.hyperbrains.hms.service.PharmacyStockService;
import com.hyperbrains.hms.service.rules.StockAvailability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PharmacyStockServiceImpl implements PharmacyStockService {

    private static final Logger LOG = LoggerFactory.getLogger(PharmacyStockServiceImpl.class);

    private final DrugRepository drugRepository;

    private final AuditLogService auditLogService;

    public PharmacyStockServiceImpl(DrugRepository drugRepository, AuditLogService auditLogService) {
        this.drugRepository = drugRepository;
        this.auditLogService = auditLogService;
    }

    @Override
    public Drug reserve(Long drugId, int quantity) {
        if (quantity <= 0) {
            // A caller asking to reserve nothing is a bug in the caller, not a clinical situation.
            throw new IllegalArgumentException("A reservation must be for at least one unit, got " + quantity);
        }

        Drug drug = drugRepository
            .findById(drugId)
            .orElseThrow(() -> BusinessRuleViolationException.of("drugNotFound", "drug", "No drug with id " + drugId));

        if (!Boolean.TRUE.equals(drug.getActive())) {
            // A withdrawn drug must not be prescribed through any route, however much stock is left.
            throw BusinessRuleViolationException.of(
                "drugNotActive",
                "drug",
                drug.getName() + " is no longer stocked and cannot be prescribed"
            );
        }

        int currentStock = drug.getCurrentStock();
        int reservedStock = drug.getReservedStock();

        if (!StockAvailability.canReserve(currentStock, reservedStock, quantity)) {
            int shortfall = StockAvailability.shortfall(currentStock, reservedStock, quantity);
            throw BusinessRuleViolationException.of(
                "insufficientStock",
                "drug",
                "%s: only %d %s available but %d requested, %d short".formatted(
                    drug.getName(),
                    StockAvailability.available(currentStock, reservedStock),
                    drug.getUnit(),
                    quantity,
                    shortfall
                )
            );
        }

        drug.setReservedStock(reservedStock + quantity);
        drug = drugRepository.save(drug);

        int stillAvailable = StockAvailability.available(drug.getCurrentStock(), drug.getReservedStock());
        auditLogService.record(
            AuditLogService.Entry.of(AuditActions.STOCK_RESERVED, "Drug", drug.getId()).withDetails(
                "%d %s set aside; %d of %d now reserved, %d still available".formatted(
                    quantity,
                    drug.getUnit(),
                    drug.getReservedStock(),
                    drug.getCurrentStock(),
                    stillAvailable
                )
            )
        );

        if (StockAvailability.belowThreshold(drug.getCurrentStock(), drug.getReservedStock(), drug.getLowStockThreshold())) {
            // Not an error: the prescription is honoured, but the shelf needs attention.
            LOG.warn(
                "Drug {} ({}) is at or below its reorder point: {} available, threshold {}",
                drug.getId(),
                drug.getName(),
                stillAvailable,
                drug.getLowStockThreshold()
            );
        }

        LOG.debug("Reserved {} {} of drug {}; reservedStock now {}", quantity, drug.getUnit(), drug.getId(), drug.getReservedStock());
        return drug;
    }

    @Override
    public Drug release(Long drugId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("A release must be for at least one unit, got " + quantity);
        }

        Drug drug = loadDrug(drugId);
        int reservedStock = drug.getReservedStock();

        if (reservedStock < quantity) {
            // Not a clinical refusal: this means something already released more than was reserved, so
            // the bookkeeping is broken. Failing loudly keeps the reservation intact rather than
            // driving reservedStock negative, which would make availability lie in the other direction.
            throw new IllegalArgumentException(
                "Cannot release %d %s of %s: only %d are reserved".formatted(quantity, drug.getUnit(), drug.getName(), reservedStock)
            );
        }

        drug.setReservedStock(reservedStock - quantity);
        drug = drugRepository.save(drug);

        auditLogService.record(
            AuditLogService.Entry.of(AuditActions.STOCK_RELEASED, "Drug", drug.getId()).withDetails(
                "%d %s given back; %d of %d now reserved, %d available".formatted(
                    quantity,
                    drug.getUnit(),
                    drug.getReservedStock(),
                    drug.getCurrentStock(),
                    StockAvailability.available(drug.getCurrentStock(), drug.getReservedStock())
                )
            )
        );

        LOG.debug("Released {} {} of drug {}; reservedStock now {}", quantity, drug.getUnit(), drug.getId(), drug.getReservedStock());
        return drug;
    }

    @Override
    public Drug receive(Long drugId, int quantity, String reference) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("A delivery must be for at least one unit, got " + quantity);
        }
        requireReference(reference, "A delivery must quote the invoice it arrived on");

        // No active check: whether a drug may be prescribed is a separate question from whether stock
        // arrived, and refusing a delivery would leave the paperwork and the shelf disagreeing. The
        // active flag stops it being prescribed, which is the thing that actually matters.
        Drug drug = loadDrug(drugId);
        drug.setCurrentStock(drug.getCurrentStock() + quantity);
        drug = drugRepository.save(drug);

        auditLogService.record(
            AuditLogService.Entry.of(AuditActions.STOCK_RECEIVED, "Drug", drug.getId())
                .withReason(reference)
                .withDetails(
                    "%d %s received; %d in stock, %d reserved, %d available".formatted(
                        quantity,
                        drug.getUnit(),
                        drug.getCurrentStock(),
                        drug.getReservedStock(),
                        StockAvailability.available(drug.getCurrentStock(), drug.getReservedStock())
                    )
                )
        );

        LOG.debug("Received {} {} of drug {}; currentStock now {}", quantity, drug.getUnit(), drug.getId(), drug.getCurrentStock());
        return drug;
    }

    @Override
    public Drug writeOff(Long drugId, int quantity, String reference) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("A write-off must be for at least one unit, got " + quantity);
        }
        requireReference(reference, "A write-off must state why the stock is unusable");

        Drug drug = loadDrug(drugId);
        int currentStock = drug.getCurrentStock();
        int reservedStock = drug.getReservedStock();
        int available = StockAvailability.available(currentStock, reservedStock);

        if (quantity > available) {
            // The reserved units are already promised to a patient, so they are not the pharmacy's to
            // throw away. If that medicine really is unusable, the prescription has to be withdrawn
            // first - which is also what puts the money side right.
            throw BusinessRuleViolationException.of(
                "cannotWriteOffReservedStock",
                "drug",
                "%s: %d %s available but %d written off. The other %d are reserved for a patient; withdraw the prescription first".formatted(
                    drug.getName(),
                    available,
                    drug.getUnit(),
                    quantity,
                    reservedStock
                )
            );
        }

        drug.setCurrentStock(currentStock - quantity);
        drug = drugRepository.save(drug);

        auditLogService.record(
            AuditLogService.Entry.of(AuditActions.STOCK_WRITTEN_OFF, "Drug", drug.getId())
                .withReason(reference)
                .withDetails(
                    "%d %s written off; %d in stock, %d reserved, %d available".formatted(
                        quantity,
                        drug.getUnit(),
                        drug.getCurrentStock(),
                        drug.getReservedStock(),
                        StockAvailability.available(drug.getCurrentStock(), drug.getReservedStock())
                    )
                )
        );

        LOG.debug("Wrote off {} {} of drug {}; currentStock now {}", quantity, drug.getUnit(), drug.getId(), drug.getCurrentStock());
        return drug;
    }

    @Override
    public Drug consume(Long drugId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("A dispensed quantity must be at least one unit, got " + quantity);
        }

        Drug drug = loadDrug(drugId);
        int reservedStock = drug.getReservedStock();

        if (reservedStock < quantity) {
            // The prescription promised these units at the moment it was written, so fewer reserved than
            // dispensed means the bookkeeping is already broken - most likely a second dispense through
            // another path. Failing loudly leaves the reservation intact rather than consuming stock some
            // other patient holds a promise against.
            throw new IllegalStateException(
                "Cannot hand over %d %s of %s: only %d are reserved".formatted(quantity, drug.getUnit(), drug.getName(), reservedStock)
            );
        }

        // No separate check against currentStock: reserve() and writeOff() both maintain
        // reservedStock <= currentStock, so a reservation this size proves the shelf can cover it.
        drug.setCurrentStock(drug.getCurrentStock() - quantity);
        drug.setReservedStock(reservedStock - quantity);
        drug = drugRepository.save(drug);

        auditLogService.record(
            AuditLogService.Entry.of(AuditActions.STOCK_DISPENSED, "Drug", drug.getId()).withDetails(
                "%d %s handed over; %d in stock, %d reserved, %d available".formatted(
                    quantity,
                    drug.getUnit(),
                    drug.getCurrentStock(),
                    drug.getReservedStock(),
                    StockAvailability.available(drug.getCurrentStock(), drug.getReservedStock())
                )
            )
        );

        LOG.debug("Consumed {} {} of drug {}; currentStock now {}", quantity, drug.getUnit(), drug.getId(), drug.getCurrentStock());
        return drug;
    }

    private Drug loadDrug(Long drugId) {
        return drugRepository
            .findById(drugId)
            .orElseThrow(() -> BusinessRuleViolationException.of("drugNotFound", "drug", "No drug with id " + drugId));
    }

    /**
     * A stock movement without a paper trail is not reconstructible later.
     *
     * <p>Checked here as well as on the DTO: bean validation only runs when the request arrives over
     * HTTP, and this is the method that actually moves the stock.
     */
    private static void requireReference(String reference, String message) {
        if (reference == null || reference.isBlank()) {
            throw BusinessRuleViolationException.of("stockReferenceRequired", "drug", message);
        }
    }
}
