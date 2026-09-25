package com.hyperbrains.hms.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hyperbrains.hms.IntegrationTest;
import com.hyperbrains.hms.domain.Drug;
import com.hyperbrains.hms.repository.DrugRepository;
import com.hyperbrains.hms.service.BusinessRuleViolationException;
import com.hyperbrains.hms.service.PharmacyStockService;
import java.math.BigDecimal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

/**
 * Integration tests for the two sanctioned stock movements: deliveries in, write-offs out.
 *
 * <p>These endpoints exist because the generated {@code /api/drugs} write had to be closed to
 * pharmacy — it can set {@code reservedStock} directly, which silently breaks a reservation. What is
 * verified here is that the replacement path keeps the one invariant that matters: reserved units are
 * never sold twice, and are never written off either.
 */
@IntegrationTest
@WithMockUser(value = "admin", authorities = "ROLE_PHARMACY")
class PharmacyStockIT {

    @Autowired
    private PharmacyStockService pharmacyStockService;

    @Autowired
    private DrugRepository drugRepository;

    private Drug drug;

    @BeforeEach
    void setUp() {
        drug = new Drug();
        drug.setName("Paracetamol");
        drug.setUnit("tablet");
        drug.setPrice(new BigDecimal("2.50"));
        drug.setCurrentStock(100);
        drug.setReservedStock(0);
        drug.setLowStockThreshold(10);
        drug.setActive(true);
        drug = drugRepository.save(drug);
    }

    @AfterEach
    void cleanup() {
        if (drug != null && drug.getId() != null) {
            drugRepository.findById(drug.getId()).ifPresent(drugRepository::delete);
        }
    }

    @Test
    void receivingIncreasesTheShelfWithoutTouchingReservations() {
        pharmacyStockService.reserve(drug.getId(), 5);

        Drug after = pharmacyStockService.receive(drug.getId(), 50, "Invoice INV-2043");

        assertThat(after.getCurrentStock()).isEqualTo(150);
        // Arriving stock must not be able to invalidate a promise already made.
        assertThat(after.getReservedStock()).isEqualTo(5);
    }

    @Test
    void aDeliveryMustQuoteTheInvoiceItArrivedOn() {
        assertThatThrownBy(() -> pharmacyStockService.receive(drug.getId(), 10, "  "))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("invoice");

        assertThat(reload().getCurrentStock()).isEqualTo(100);
    }

    @Test
    void aDeliveryForAnUnknownDrugIsRefused() {
        assertThatThrownBy(() -> pharmacyStockService.receive(999_999_999L, 1, "Invoice INV-1"))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("No drug with id");
    }

    @Test
    void writingOffReducesTheShelf() {
        Drug after = pharmacyStockService.writeOff(drug.getId(), 12, "Broken blister packs");

        assertThat(after.getCurrentStock()).isEqualTo(88);
        assertThat(after.getReservedStock()).isZero();
    }

    /**
     * The invariant this endpoint exists to protect. Units already promised to a patient are not the
     * pharmacy's to throw away, and letting them go would leave a patient with a paid prescription
     * that can never be filled.
     */
    @Test
    void writingOffMedicineAlreadyPromisedToAPatientIsRefused() {
        pharmacyStockService.reserve(drug.getId(), 96);

        assertThatThrownBy(() -> pharmacyStockService.writeOff(drug.getId(), 10, "Water damage"))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("withdraw the prescription first");

        // Nothing moved: the shelf is intact and so is the reservation.
        assertThat(reload().getCurrentStock()).isEqualTo(100);
        assertThat(reload().getReservedStock()).isEqualTo(96);
    }

    /** The whole available quantity may be written off, so the boundary itself is not off by one. */
    @Test
    void writingOffExactlyWhatIsAvailableIsAllowed() {
        pharmacyStockService.reserve(drug.getId(), 96);

        Drug after = pharmacyStockService.writeOff(drug.getId(), 4, "Expired");

        assertThat(after.getCurrentStock()).isEqualTo(96);
        assertThat(after.getReservedStock()).isEqualTo(96);
    }

    @Test
    void aWriteOffMustStateWhy() {
        assertThatThrownBy(() -> pharmacyStockService.writeOff(drug.getId(), 5, null))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("must state why");

        assertThat(reload().getCurrentStock()).isEqualTo(100);
    }

    private Drug reload() {
        return drugRepository.findById(drug.getId()).orElseThrow();
    }
}
