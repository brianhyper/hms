package com.hyperbrains.hms.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.hyperbrains.hms.domain.WardCover} entity.
 */
@Schema(
    description = "Which doctor is responsible for which ward, for a period. Without this the\n\"a doctor sees admissions on a ward they are covering\" rule has no data to read."
)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class WardCoverDTO implements Serializable {

    private Long id;

    @NotNull
    private Instant coversFrom;

    private Instant coversTo;

    @Size(max = 500)
    private String note;

    @NotNull
    private UserDTO doctor;

    @NotNull
    private WardDTO ward;

    @NotNull
    private UserDTO assignedBy;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getCoversFrom() {
        return coversFrom;
    }

    public void setCoversFrom(Instant coversFrom) {
        this.coversFrom = coversFrom;
    }

    public Instant getCoversTo() {
        return coversTo;
    }

    public void setCoversTo(Instant coversTo) {
        this.coversTo = coversTo;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public UserDTO getDoctor() {
        return doctor;
    }

    public void setDoctor(UserDTO doctor) {
        this.doctor = doctor;
    }

    public WardDTO getWard() {
        return ward;
    }

    public void setWard(WardDTO ward) {
        this.ward = ward;
    }

    public UserDTO getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(UserDTO assignedBy) {
        this.assignedBy = assignedBy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof WardCoverDTO)) {
            return false;
        }

        WardCoverDTO wardCoverDTO = (WardCoverDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, wardCoverDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "WardCoverDTO{" +
            "id=" + getId() +
            ", coversFrom='" + getCoversFrom() + "'" +
            ", coversTo='" + getCoversTo() + "'" +
            ", note='" + getNote() + "'" +
            ", doctor=" + getDoctor() +
            ", ward=" + getWard() +
            ", assignedBy=" + getAssignedBy() +
            "}";
    }
}
