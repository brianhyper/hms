package com.hyperbrains.hms.service.dto;

import com.hyperbrains.hms.domain.enumeration.AdmissionStatus;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.hyperbrains.hms.domain.Admission} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AdmissionDTO implements Serializable {

    private Long id;

    @NotNull
    private Instant admittedAt;

    @NotNull
    @Size(max = 10000)
    private String admissionReason;

    @NotNull
    private AdmissionStatus status;

    private Instant dischargedAt;

    @Size(max = 10000)
    private String dischargeNote;

    @NotNull
    private VisitDTO visit;

    private BedDTO bed;

    @NotNull
    private UserDTO admittingDoctor;

    @NotNull
    private UserDTO primaryDoctor;

    private UserDTO dischargedByDoctor;

    private UserDTO dischargedByNurse;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getAdmittedAt() {
        return admittedAt;
    }

    public void setAdmittedAt(Instant admittedAt) {
        this.admittedAt = admittedAt;
    }

    public String getAdmissionReason() {
        return admissionReason;
    }

    public void setAdmissionReason(String admissionReason) {
        this.admissionReason = admissionReason;
    }

    public AdmissionStatus getStatus() {
        return status;
    }

    public void setStatus(AdmissionStatus status) {
        this.status = status;
    }

    public Instant getDischargedAt() {
        return dischargedAt;
    }

    public void setDischargedAt(Instant dischargedAt) {
        this.dischargedAt = dischargedAt;
    }

    public String getDischargeNote() {
        return dischargeNote;
    }

    public void setDischargeNote(String dischargeNote) {
        this.dischargeNote = dischargeNote;
    }

    public VisitDTO getVisit() {
        return visit;
    }

    public void setVisit(VisitDTO visit) {
        this.visit = visit;
    }

    public BedDTO getBed() {
        return bed;
    }

    public void setBed(BedDTO bed) {
        this.bed = bed;
    }

    public UserDTO getAdmittingDoctor() {
        return admittingDoctor;
    }

    public void setAdmittingDoctor(UserDTO admittingDoctor) {
        this.admittingDoctor = admittingDoctor;
    }

    public UserDTO getPrimaryDoctor() {
        return primaryDoctor;
    }

    public void setPrimaryDoctor(UserDTO primaryDoctor) {
        this.primaryDoctor = primaryDoctor;
    }

    public UserDTO getDischargedByDoctor() {
        return dischargedByDoctor;
    }

    public void setDischargedByDoctor(UserDTO dischargedByDoctor) {
        this.dischargedByDoctor = dischargedByDoctor;
    }

    public UserDTO getDischargedByNurse() {
        return dischargedByNurse;
    }

    public void setDischargedByNurse(UserDTO dischargedByNurse) {
        this.dischargedByNurse = dischargedByNurse;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AdmissionDTO)) {
            return false;
        }

        AdmissionDTO admissionDTO = (AdmissionDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, admissionDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AdmissionDTO{" +
            "id=" + getId() +
            ", admittedAt='" + getAdmittedAt() + "'" +
            ", admissionReason='" + getAdmissionReason() + "'" +
            ", status='" + getStatus() + "'" +
            ", dischargedAt='" + getDischargedAt() + "'" +
            ", dischargeNote='" + getDischargeNote() + "'" +
            ", visit=" + getVisit() +
            ", bed=" + getBed() +
            ", admittingDoctor=" + getAdmittingDoctor() +
            ", primaryDoctor=" + getPrimaryDoctor() +
            ", dischargedByDoctor=" + getDischargedByDoctor() +
            ", dischargedByNurse=" + getDischargedByNurse() +
            "}";
    }
}
