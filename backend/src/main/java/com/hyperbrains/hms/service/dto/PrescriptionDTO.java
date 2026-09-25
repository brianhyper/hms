package com.hyperbrains.hms.service.dto;

import com.hyperbrains.hms.domain.enumeration.PrescriptionSource;
import com.hyperbrains.hms.domain.enumeration.PrescriptionStatus;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.hyperbrains.hms.domain.Prescription} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class PrescriptionDTO implements Serializable {

    private Long id;

    @NotNull
    private PrescriptionSource source;

    @Size(max = 255)
    private String prescribingSource;

    @NotNull
    private PrescriptionStatus status;

    /** When it was written. Drives how long the pharmacy queue shows a patient has been waiting. */
    @NotNull
    private Instant createdAt;

    private VisitDTO visit;

    private UserDTO doctor;

    private int version;

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PrescriptionSource getSource() {
        return source;
    }

    public void setSource(PrescriptionSource source) {
        this.source = source;
    }

    public String getPrescribingSource() {
        return prescribingSource;
    }

    public void setPrescribingSource(String prescribingSource) {
        this.prescribingSource = prescribingSource;
    }

    public PrescriptionStatus getStatus() {
        return status;
    }

    public void setStatus(PrescriptionStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public VisitDTO getVisit() {
        return visit;
    }

    public void setVisit(VisitDTO visit) {
        this.visit = visit;
    }

    public UserDTO getDoctor() {
        return doctor;
    }

    public void setDoctor(UserDTO doctor) {
        this.doctor = doctor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PrescriptionDTO)) {
            return false;
        }

        PrescriptionDTO prescriptionDTO = (PrescriptionDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, prescriptionDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "PrescriptionDTO{" +
            "id=" + getId() +
            ", source='" + getSource() + "'" +
            ", prescribingSource='" + getPrescribingSource() + "'" +
            ", status='" + getStatus() + "'" +
            ", visit=" + getVisit() +
            ", doctor=" + getDoctor() +
            "}";
    }
}
