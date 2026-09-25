package com.hyperbrains.hms.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.hyperbrains.hms.domain.Dispense} entity.
 */
@Schema(description = "A dispensing transaction. Multiple transactions allow partial dispensing.")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class DispenseDTO implements Serializable {

    private Long id;

    @NotNull
    private Instant dispensedAt;
    private String note;

    @NotNull
    private PrescriptionDTO prescription;

    @NotNull
    private UserDTO recordedBy;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getDispensedAt() {
        return dispensedAt;
    }

    public void setDispensedAt(Instant dispensedAt) {
        this.dispensedAt = dispensedAt;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public PrescriptionDTO getPrescription() {
        return prescription;
    }

    public void setPrescription(PrescriptionDTO prescription) {
        this.prescription = prescription;
    }

    public UserDTO getRecordedBy() {
        return recordedBy;
    }

    public void setRecordedBy(UserDTO recordedBy) {
        this.recordedBy = recordedBy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DispenseDTO)) {
            return false;
        }

        DispenseDTO dispenseDTO = (DispenseDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, dispenseDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "DispenseDTO{" +
            "id=" + getId() +
            ", dispensedAt='" + getDispensedAt() + "'" +
            ", note='" + getNote() + "'" +
            ", prescription=" + getPrescription() +
            ", recordedBy=" + getRecordedBy() +
            "}";
    }
}
