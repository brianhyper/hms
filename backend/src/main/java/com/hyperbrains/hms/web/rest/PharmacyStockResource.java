package com.hyperbrains.hms.web.rest;

import com.hyperbrains.hms.domain.Drug;
import com.hyperbrains.hms.service.PharmacyStockService;
import com.hyperbrains.hms.service.dto.view.StockMovementRequestDTO;
import com.hyperbrains.hms.service.rules.StockAvailability;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * The sanctioned way for pharmacy stock to change outside prescribing and dispensing.
 *
 * <p>This exists because the generated {@code /api/drugs} write had to be closed to pharmacy: it can
 * set {@code reservedStock} directly, which silently breaks a reservation this system made. Stock
 * still has to be received and written off, so those two acts get their own endpoints, where the
 * reservation invariant is enforced rather than bypassed.
 */
@RestController
@RequestMapping("/api/pharmacy-stock")
public class PharmacyStockResource {

    private final PharmacyStockService pharmacyStockService;

    public PharmacyStockResource(PharmacyStockService pharmacyStockService) {
        this.pharmacyStockService = pharmacyStockService;
    }

    /** A delivery arriving: stock goes up, and reserved quantities are untouched. */
    @PostMapping("/{drugId}/receive")
    public ResponseEntity<Map<String, Object>> receive(
        @PathVariable Long drugId,
        @Valid @RequestBody StockMovementRequestDTO request
    ) {
        return ResponseEntity.ok(view(pharmacyStockService.receive(drugId, request.getQuantity(), request.getReference())));
    }

    /**
     * Stock leaving the shelf for a reason other than dispensing.
     *
     * <p>Refused when it would eat into units already reserved for a patient — that medicine is owed,
     * so the prescription has to be withdrawn first.
     */
    @PostMapping("/{drugId}/write-off")
    public ResponseEntity<Map<String, Object>> writeOff(
        @PathVariable Long drugId,
        @Valid @RequestBody StockMovementRequestDTO request
    ) {
        return ResponseEntity.ok(view(pharmacyStockService.writeOff(drugId, request.getQuantity(), request.getReference())));
    }

    /**
     * Only the figures a counter needs, deliberately not the whole {@code Drug} entity.
     *
     * <p>Returning the entity would expose the version number and every catalogue field for what is
     * really a four-number answer, and would make this response change shape whenever the drug
     * catalogue does.
     */
    private static Map<String, Object> view(Drug drug) {
        return Map.of(
            "drugId",
            drug.getId(),
            "name",
            drug.getName(),
            "unit",
            drug.getUnit(),
            "currentStock",
            drug.getCurrentStock(),
            "reservedStock",
            drug.getReservedStock(),
            "available",
            StockAvailability.available(drug.getCurrentStock(), drug.getReservedStock()),
            "lowStockThreshold",
            drug.getLowStockThreshold(),
            "recordedAt",
            Instant.now()
        );
    }
}
