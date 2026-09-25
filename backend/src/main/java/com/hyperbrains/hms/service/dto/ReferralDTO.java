package com.hyperbrains.hms.service.dto;

import com.hyperbrains.hms.domain.enumeration.ReferralStatus;
import com.hyperbrains.hms.domain.enumeration.ReferralType;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.hyperbrains.hms.domain.Referral} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ReferralDTO implements Serializable {

    private Long id;

    @NotNull
    private ReferralType type;

    @NotNull
    @Size(max = 255)
    private String destination;

    @Size(max = 254)
    private String destinationEmail;
    private String reason;
    private String notes;

    @NotNull
    private ReferralStatus status;

    @NotNull
    private Instant createdAt;

    @NotNull
    private VisitDTO visit;

    @NotNull
    private UserDTO referredBy;

    private DepartmentDTO department;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ReferralType getType() {
        return type;
    }

    public void setType(ReferralType type) {
        this.type = type;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getDestinationEmail() {
        return destinationEmail;
    }

    public void setDestinationEmail(String destinationEmail) {
        this.destinationEmail = destinationEmail;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public ReferralStatus getStatus() {
        return status;
    }

    public void setStatus(ReferralStatus status) {
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

    public UserDTO getReferredBy() {
        return referredBy;
    }

    public void setReferredBy(UserDTO referredBy) {
        this.referredBy = referredBy;
    }

    public DepartmentDTO getDepartment() {
        return department;
    }

    public void setDepartment(DepartmentDTO department) {
        this.department = department;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ReferralDTO)) {
            return false;
        }

        ReferralDTO referralDTO = (ReferralDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, referralDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ReferralDTO{" +
            "id=" + getId() +
            ", type='" + getType() + "'" +
            ", destination='" + getDestination() + "'" +
            ", destinationEmail='" + getDestinationEmail() + "'" +
            ", reason='" + getReason() + "'" +
            ", notes='" + getNotes() + "'" +
            ", status='" + getStatus() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            ", visit=" + getVisit() +
            ", referredBy=" + getReferredBy() +
            ", department=" + getDepartment() +
            "}";
    }
}
