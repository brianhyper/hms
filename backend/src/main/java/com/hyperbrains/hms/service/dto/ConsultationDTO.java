package com.hyperbrains.hms.service.dto;

import com.hyperbrains.hms.domain.enumeration.ConsultationStatus;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A DTO for the {@link com.hyperbrains.hms.domain.Consultation} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ConsultationDTO implements Serializable {

    private Long id;
    private String presentingComplaint;
    private String examinationFindings;
    private String diagnosisOther;
    private String observations;
    private String followUpInstructions;

    @NotNull
    private ConsultationStatus status;

    @NotNull
    private Instant startedAt;

    private Instant completedAt;

    @NotNull
    private UserDTO doctor;

    private Set<DiagnosisDTO> diagnoseses = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPresentingComplaint() {
        return presentingComplaint;
    }

    public void setPresentingComplaint(String presentingComplaint) {
        this.presentingComplaint = presentingComplaint;
    }

    public String getExaminationFindings() {
        return examinationFindings;
    }

    public void setExaminationFindings(String examinationFindings) {
        this.examinationFindings = examinationFindings;
    }

    public String getDiagnosisOther() {
        return diagnosisOther;
    }

    public void setDiagnosisOther(String diagnosisOther) {
        this.diagnosisOther = diagnosisOther;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public String getFollowUpInstructions() {
        return followUpInstructions;
    }

    public void setFollowUpInstructions(String followUpInstructions) {
        this.followUpInstructions = followUpInstructions;
    }

    public ConsultationStatus getStatus() {
        return status;
    }

    public void setStatus(ConsultationStatus status) {
        this.status = status;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public UserDTO getDoctor() {
        return doctor;
    }

    public void setDoctor(UserDTO doctor) {
        this.doctor = doctor;
    }

    public Set<DiagnosisDTO> getDiagnoseses() {
        return diagnoseses;
    }

    public void setDiagnoseses(Set<DiagnosisDTO> diagnoseses) {
        this.diagnoseses = diagnoseses;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ConsultationDTO)) {
            return false;
        }

        ConsultationDTO consultationDTO = (ConsultationDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, consultationDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ConsultationDTO{" +
            "id=" + getId() +
            ", presentingComplaint='" + getPresentingComplaint() + "'" +
            ", examinationFindings='" + getExaminationFindings() + "'" +
            ", diagnosisOther='" + getDiagnosisOther() + "'" +
            ", observations='" + getObservations() + "'" +
            ", followUpInstructions='" + getFollowUpInstructions() + "'" +
            ", status='" + getStatus() + "'" +
            ", startedAt='" + getStartedAt() + "'" +
            ", completedAt='" + getCompletedAt() + "'" +
            ", doctor=" + getDoctor() +
            ", diagnoseses=" + getDiagnoseses() +
            "}";
    }
}
