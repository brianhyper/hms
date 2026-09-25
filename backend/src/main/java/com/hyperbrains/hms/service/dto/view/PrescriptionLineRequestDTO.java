package com.hyperbrains.hms.service.dto.view;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * One drug on a prescription.
 *
 * <p>{@code drugId} is a catalogue reference, not free text: the price and the stock that gets
 * reserved both come from that row, so a line naming a drug that does not exist cannot be priced or
 * promised.
 */
public class PrescriptionLineRequestDTO implements Serializable {

    @NotNull
    private Long drugId;

    /** The instruction, e.g. "1 tablet twice daily". */
    @NotBlank
    @Size(max = 200)
    private String dosage;

    /** e.g. "5 days". */
    @NotBlank
    @Size(max = 100)
    private String duration;

    /** How many units to set aside and charge for. */
    @NotNull
    @Min(1)
    private Integer quantity;

    public Long getDrugId() {
        return drugId;
    }

    public void setDrugId(Long drugId) {
        this.drugId = drugId;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
