package com.hyperbrains.hms.service.dto.view;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/** One prescribed line being handed over, and how much of it. */
public class DispenseLineRequestDTO implements Serializable {

    @NotNull
    private Long prescriptionLineId;

    @NotNull
    @Min(1)
    private Integer quantity;

    public Long getPrescriptionLineId() {
        return prescriptionLineId;
    }

    public void setPrescriptionLineId(Long prescriptionLineId) {
        this.prescriptionLineId = prescriptionLineId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
