package com.hyperbrains.hms.service.dto.view;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * A correction to what is physically on the shelf.
 *
 * <p>Two acts share this shape because they are the same act with opposite signs: a delivery adds
 * units, a write-off removes them. Both need a reference, and for opposite reasons — a delivery needs
 * the invoice it can be reconciled against, a write-off needs the reason it will be defended with.
 */
public class StockMovementRequestDTO implements Serializable {

    @NotNull
    @Min(1)
    private Integer quantity;

    /** Supplier invoice for a delivery; the stated cause for a write-off. */
    @NotBlank
    @Size(max = 255)
    private String reference;

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }
}
