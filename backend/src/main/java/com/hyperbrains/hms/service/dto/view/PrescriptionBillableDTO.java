package com.hyperbrains.hms.service.dto.view;

import com.hyperbrains.hms.domain.PrescriptionLine;
import com.hyperbrains.hms.domain.enumeration.PrescriptionStatus;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * The narrow view Finance needs to bill for medicine: how many, of what, at what price.
 *
 * <p>Deliberately excludes {@code dosage} and {@code duration}. Those are the prescriber's clinical
 * instructions — "1 tablet twice daily for 5 days" is a treatment, not a price. Finance reconciling
 * an invoice needs the drug, the quantity and the money, and nothing about how the patient is
 * supposed to take it. A structural test keeps that boundary from eroding.
 */
public class PrescriptionBillableDTO implements Serializable {

    private Long prescriptionId;

    private Long lineId;

    private Long visitId;

    private String drugName;

    private Integer quantity;

    private BigDecimal unitPrice;

    private BigDecimal amount;

    private PrescriptionStatus status;

    public static PrescriptionBillableDTO from(PrescriptionLine line, Long prescriptionId, Long visitId) {
        PrescriptionBillableDTO dto = new PrescriptionBillableDTO();
        dto.prescriptionId = prescriptionId;
        dto.visitId = visitId;
        dto.lineId = line.getId();
        dto.quantity = line.getQuantity();
        if (line.getDrug() != null) {
            dto.drugName = line.getDrug().getName();
            dto.unitPrice = line.getDrug().getPrice();
            if (line.getDrug().getPrice() != null && line.getQuantity() != null) {
                dto.amount = line.getDrug().getPrice().multiply(BigDecimal.valueOf(line.getQuantity()));
            }
        }
        if (line.getPrescription() != null) {
            dto.status = line.getPrescription().getStatus();
        }
        return dto;
    }

    public Long getPrescriptionId() {
        return prescriptionId;
    }

    public void setPrescriptionId(Long prescriptionId) {
        this.prescriptionId = prescriptionId;
    }

    public Long getLineId() {
        return lineId;
    }

    public void setLineId(Long lineId) {
        this.lineId = lineId;
    }

    public Long getVisitId() {
        return visitId;
    }

    public void setVisitId(Long visitId) {
        this.visitId = visitId;
    }

    public String getDrugName() {
        return drugName;
    }

    public void setDrugName(String drugName) {
        this.drugName = drugName;
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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public PrescriptionStatus getStatus() {
        return status;
    }

    public void setStatus(PrescriptionStatus status) {
        this.status = status;
    }
}
