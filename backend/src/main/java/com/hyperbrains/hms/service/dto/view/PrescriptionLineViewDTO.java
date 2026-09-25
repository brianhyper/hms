package com.hyperbrains.hms.service.dto.view;

import com.hyperbrains.hms.domain.Drug;
import com.hyperbrains.hms.domain.PrescriptionLine;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * One drug on a prescription, as the clinician and the pharmacy see it.
 *
 * <p>Carries the drug's unit and the line total because those are what the counter actually works
 * with — "20 tablets" is dispensable, "20" is not.
 */
public class PrescriptionLineViewDTO implements Serializable {

    private Long lineId;

    private Long drugId;

    private String drugName;

    private String unit;

    private String dosage;

    private String duration;

    private Integer quantity;

    private BigDecimal unitPrice;

    private BigDecimal lineTotal;

    public static PrescriptionLineViewDTO from(PrescriptionLine line) {
        PrescriptionLineViewDTO dto = new PrescriptionLineViewDTO();
        dto.lineId = line.getId();
        dto.dosage = line.getDosage();
        dto.duration = line.getDuration();
        dto.quantity = line.getQuantity();

        Drug drug = line.getDrug();
        if (drug != null) {
            dto.drugId = drug.getId();
            dto.drugName = drug.getName();
            dto.unit = drug.getUnit();
            dto.unitPrice = drug.getPrice();
            if (drug.getPrice() != null && line.getQuantity() != null) {
                dto.lineTotal = drug.getPrice().multiply(BigDecimal.valueOf(line.getQuantity()));
            }
        }
        return dto;
    }

    public Long getLineId() {
        return lineId;
    }

    public void setLineId(Long lineId) {
        this.lineId = lineId;
    }

    public Long getDrugId() {
        return drugId;
    }

    public void setDrugId(Long drugId) {
        this.drugId = drugId;
    }

    public String getDrugName() {
        return drugName;
    }

    public void setDrugName(String drugName) {
        this.drugName = drugName;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
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

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    public void setLineTotal(BigDecimal lineTotal) {
        this.lineTotal = lineTotal;
    }
}
