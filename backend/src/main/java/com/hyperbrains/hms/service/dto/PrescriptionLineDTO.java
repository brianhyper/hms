package com.hyperbrains.hms.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link com.hyperbrains.hms.domain.PrescriptionLine} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class PrescriptionLineDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 200)
    private String dosage;

    @NotNull
    @Size(max = 100)
    private String duration;

    @NotNull
    @Min(value = 1)
    private Integer quantity;

    @NotNull
    private PrescriptionDTO prescription;

    @NotNull
    private DrugDTO drug;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public PrescriptionDTO getPrescription() {
        return prescription;
    }

    public void setPrescription(PrescriptionDTO prescription) {
        this.prescription = prescription;
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
        if (!(o instanceof PrescriptionLineDTO)) {
            return false;
        }

        PrescriptionLineDTO prescriptionLineDTO = (PrescriptionLineDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, prescriptionLineDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "PrescriptionLineDTO{" +
            "id=" + getId() +
            ", dosage='" + getDosage() + "'" +
            ", duration='" + getDuration() + "'" +
            ", quantity=" + getQuantity() +
            ", prescription=" + getPrescription() +
            ", drug=" + getDrug() +
            "}";
    }
}
