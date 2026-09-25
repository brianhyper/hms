package com.hyperbrains.hms.service.dto;

import com.hyperbrains.hms.domain.enumeration.VisitPriority;
import com.hyperbrains.hms.domain.enumeration.VisitStatus;
import com.hyperbrains.hms.domain.enumeration.VisitType;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.hyperbrains.hms.domain.Visit} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class VisitDTO implements Serializable {

    private Long id;

    @NotNull
    private VisitType type;

    @NotNull
    private VisitPriority priority;
    private String reasonForVisit;

    @NotNull
    private VisitStatus status;
    private String queueSkipReason;

    @NotNull
    private Instant createdAt;

    private Instant startedVitalsAt;

    private Instant startedConsultationAt;

    private Instant closedAt;

    private VitalSignsDTO vitals;

    private ConsultationDTO consultation;

    private BillDTO bill;

    @NotNull
    private PatientDTO patient;

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

    public VisitType getType() {
        return type;
    }

    public void setType(VisitType type) {
        this.type = type;
    }

    public VisitPriority getPriority() {
        return priority;
    }

    public void setPriority(VisitPriority priority) {
        this.priority = priority;
    }

    public String getReasonForVisit() {
        return reasonForVisit;
    }

    public void setReasonForVisit(String reasonForVisit) {
        this.reasonForVisit = reasonForVisit;
    }

    public VisitStatus getStatus() {
        return status;
    }

    public void setStatus(VisitStatus status) {
        this.status = status;
    }

    public String getQueueSkipReason() {
        return queueSkipReason;
    }

    public void setQueueSkipReason(String queueSkipReason) {
        this.queueSkipReason = queueSkipReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getStartedVitalsAt() {
        return startedVitalsAt;
    }

    public void setStartedVitalsAt(Instant startedVitalsAt) {
        this.startedVitalsAt = startedVitalsAt;
    }

    public Instant getStartedConsultationAt() {
        return startedConsultationAt;
    }

    public void setStartedConsultationAt(Instant startedConsultationAt) {
        this.startedConsultationAt = startedConsultationAt;
    }

    public Instant getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(Instant closedAt) {
        this.closedAt = closedAt;
    }

    public VitalSignsDTO getVitals() {
        return vitals;
    }

    public void setVitals(VitalSignsDTO vitals) {
        this.vitals = vitals;
    }

    public ConsultationDTO getConsultation() {
        return consultation;
    }

    public void setConsultation(ConsultationDTO consultation) {
        this.consultation = consultation;
    }

    public BillDTO getBill() {
        return bill;
    }

    public void setBill(BillDTO bill) {
        this.bill = bill;
    }

    public PatientDTO getPatient() {
        return patient;
    }

    public void setPatient(PatientDTO patient) {
        this.patient = patient;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VisitDTO)) {
            return false;
        }

        VisitDTO visitDTO = (VisitDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, visitDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "VisitDTO{" +
            "id=" + getId() +
            ", type='" + getType() + "'" +
            ", priority='" + getPriority() + "'" +
            ", reasonForVisit='" + getReasonForVisit() + "'" +
            ", status='" + getStatus() + "'" +
            ", queueSkipReason='" + getQueueSkipReason() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            ", startedVitalsAt='" + getStartedVitalsAt() + "'" +
            ", startedConsultationAt='" + getStartedConsultationAt() + "'" +
            ", closedAt='" + getClosedAt() + "'" +
            ", vitals=" + getVitals() +
            ", consultation=" + getConsultation() +
            ", bill=" + getBill() +
            ", patient=" + getPatient() +
            "}";
    }
}
