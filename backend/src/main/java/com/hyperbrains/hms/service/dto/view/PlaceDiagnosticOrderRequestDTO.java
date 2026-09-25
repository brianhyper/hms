package com.hyperbrains.hms.service.dto.view;

import com.hyperbrains.hms.domain.enumeration.OrderType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * A doctor ordering a test.
 *
 * <p>The order must name a catalogue entry matching its type, because that entry is what the price
 * comes from — an order with no catalogue entry behind it cannot be charged for, and a bill with an
 * unpriced line is worse than a refused order.
 */
public class PlaceDiagnosticOrderRequestDTO implements Serializable {

    @NotNull
    private OrderType type;

    private Long labTestId;

    private Long radiologyExamId;

    /** Clinical context for whoever performs the test, e.g. "fasting sample". */
    @Size(max = 10000)
    private String notes;

    /**
     * Conditional validation expressed as bean validation rather than a service check, so the client
     * gets a normal 400 with a field error instead of a hand-rolled error shape.
     */
    @AssertTrue(message = "Supply the catalogue entry matching the order type: labTestId for LAB, radiologyExamId for RADIOLOGY")
    public boolean isCatalogueEntryConsistent() {
        if (type == null) {
            return true;
        }
        return switch (type) {
            case LAB -> labTestId != null && radiologyExamId == null;
            case RADIOLOGY -> radiologyExamId != null && labTestId == null;
        };
    }

    public OrderType getType() {
        return type;
    }

    public void setType(OrderType type) {
        this.type = type;
    }

    public Long getLabTestId() {
        return labTestId;
    }

    public void setLabTestId(Long labTestId) {
        this.labTestId = labTestId;
    }

    public Long getRadiologyExamId() {
        return radiologyExamId;
    }

    public void setRadiologyExamId(Long radiologyExamId) {
        this.radiologyExamId = radiologyExamId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
