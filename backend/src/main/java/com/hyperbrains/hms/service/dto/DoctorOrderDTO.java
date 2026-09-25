package com.hyperbrains.hms.service.dto;

import com.hyperbrains.hms.domain.enumeration.DoctorOrderRecurrence;
import com.hyperbrains.hms.domain.enumeration.DoctorOrderStatus;
import com.hyperbrains.hms.domain.enumeration.DoctorOrderType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.hyperbrains.hms.domain.DoctorOrder} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class DoctorOrderDTO implements Serializable {

    private Long id;

    @NotNull
    private Instant orderedAt;

    @NotNull
    private DoctorOrderType type;

    @NotNull
    private DoctorOrderRecurrence recurrence;

    @Size(max = 200)
    @Schema(description = "Free text such as \"q4h\" — deliberately not a scheduling DSL.")
    private String frequency;

    private Instant endDate;

    @NotNull
    @Size(max = 10000)
    @Schema(description = "What is to be done: the instruction itself, or the drug and dose.", requiredMode = Schema.RequiredMode.REQUIRED)
    private String details;

    @NotNull
    private DoctorOrderStatus status;

    private Instant cancelledAt;

    @Size(max = 10000)
    private String cancelReason;

    @NotNull
    private AdmissionDTO admission;

    @NotNull
    private UserDTO orderedBy;

    private UserDTO cancelledBy;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getOrderedAt() {
        return orderedAt;
    }

    public void setOrderedAt(Instant orderedAt) {
        this.orderedAt = orderedAt;
    }

    public DoctorOrderType getType() {
        return type;
    }

    public void setType(DoctorOrderType type) {
        this.type = type;
    }

    public DoctorOrderRecurrence getRecurrence() {
        return recurrence;
    }

    public void setRecurrence(DoctorOrderRecurrence recurrence) {
        this.recurrence = recurrence;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public Instant getEndDate() {
        return endDate;
    }

    public void setEndDate(Instant endDate) {
        this.endDate = endDate;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public DoctorOrderStatus getStatus() {
        return status;
    }

    public void setStatus(DoctorOrderStatus status) {
        this.status = status;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(Instant cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public AdmissionDTO getAdmission() {
        return admission;
    }

    public void setAdmission(AdmissionDTO admission) {
        this.admission = admission;
    }

    public UserDTO getOrderedBy() {
        return orderedBy;
    }

    public void setOrderedBy(UserDTO orderedBy) {
        this.orderedBy = orderedBy;
    }

    public UserDTO getCancelledBy() {
        return cancelledBy;
    }

    public void setCancelledBy(UserDTO cancelledBy) {
        this.cancelledBy = cancelledBy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DoctorOrderDTO)) {
            return false;
        }

        DoctorOrderDTO doctorOrderDTO = (DoctorOrderDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, doctorOrderDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "DoctorOrderDTO{" +
            "id=" + getId() +
            ", orderedAt='" + getOrderedAt() + "'" +
            ", type='" + getType() + "'" +
            ", recurrence='" + getRecurrence() + "'" +
            ", frequency='" + getFrequency() + "'" +
            ", endDate='" + getEndDate() + "'" +
            ", details='" + getDetails() + "'" +
            ", status='" + getStatus() + "'" +
            ", cancelledAt='" + getCancelledAt() + "'" +
            ", cancelReason='" + getCancelReason() + "'" +
            ", admission=" + getAdmission() +
            ", orderedBy=" + getOrderedBy() +
            ", cancelledBy=" + getCancelledBy() +
            "}";
    }
}
