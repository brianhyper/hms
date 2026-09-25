package com.hyperbrains.hms.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link com.hyperbrains.hms.domain.DispenseLine} entity.
 */
@Schema(description = "Actual item dispensed. The drug may differ from PrescriptionLine.drug when\nan authorized substitution occurs.")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class DispenseLineDTO implements Serializable {

    private Long id;

    @NotNull
    @Min(value = 1)
    private Integer quantity;
    private String substitutionReason;

    @NotNull
    private DispenseDTO dispense;

    @NotNull
    private PrescriptionLineDTO prescriptionLine;

    @NotNull
    private DrugDTO drug;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getSubstitutionReason() {
        return substitutionReason;
    }

    public void setSubstitutionReason(String substitutionReason) {
        this.substitutionReason = substitutionReason;
    }

    public DispenseDTO getDispense() {
        return dispense;
    }

    public void setDispense(DispenseDTO dispense) {
        this.dispense = dispense;
    }

    public PrescriptionLineDTO getPrescriptionLine() {
        return prescriptionLine;
    }

    public void setPrescriptionLine(PrescriptionLineDTO prescriptionLine) {
        this.prescriptionLine = prescriptionLine;
    }

    public DrugDTO getDrug() {
        return drug;
    }

    public void setDrug(DrugDTO drug) {
        this.drug = drug;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DispenseLineDTO)) {
            return false;
        }

        DispenseLineDTO dispenseLineDTO = (DispenseLineDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, dispenseLineDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "DispenseLineDTO{" +
            "id=" + getId() +
            ", quantity=" + getQuantity() +
            ", substitutionReason='" + getSubstitutionReason() + "'" +
            ", dispense=" + getDispense() +
            ", prescriptionLine=" + getPrescriptionLine() +
            ", drug=" + getDrug() +
            "}";
    }
}
