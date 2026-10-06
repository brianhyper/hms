package com.hyperbrains.hms.service.dto;

import com.hyperbrains.hms.domain.enumeration.ShiftType;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

/**
 * A DTO for the {@link com.hyperbrains.hms.domain.Shift} entity.
 *
 * <p>The person on duty goes out as an id and nothing else, like every other reference in the model: the roster view
 * that a ward reads is a separate, purpose-built read, because a shift row carries an employment record's id and a
 * staff file must not travel with it.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ShiftDTO implements Serializable {

    private Long id;

    @NotNull
    private LocalDate shiftDate;

    @NotNull
    private ShiftType shiftType;

    @NotNull
    private LocalTime startsAt;

    @NotNull
    private LocalTime endsAt;

    @Size(max = 500)
    private String note;

    @NotNull
    private StaffRecordDTO staffRecord;

    private WardDTO ward;

    /**
     * Who wrote the roster.
     *
     * <p>Stamped by the service from the authenticated caller and never taken from the request — deliberately not
     * {@code @NotNull} here, because a caller that has to supply it could also supply somebody else's name. An edit
     * to a shift re-writes the shift, not its author.
     */
    private UserDTO createdBy;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getShiftDate() {
        return shiftDate;
    }

    public void setShiftDate(LocalDate shiftDate) {
        this.shiftDate = shiftDate;
    }

    public ShiftType getShiftType() {
        return shiftType;
    }

    public void setShiftType(ShiftType shiftType) {
        this.shiftType = shiftType;
    }

    public LocalTime getStartsAt() {
        return startsAt;
    }

    public void setStartsAt(LocalTime startsAt) {
        this.startsAt = startsAt;
    }

    public LocalTime getEndsAt() {
        return endsAt;
    }

    public void setEndsAt(LocalTime endsAt) {
        this.endsAt = endsAt;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public StaffRecordDTO getStaffRecord() {
        return staffRecord;
    }

    public void setStaffRecord(StaffRecordDTO staffRecord) {
        this.staffRecord = staffRecord;
    }

    public WardDTO getWard() {
        return ward;
    }

    public void setWard(WardDTO ward) {
        this.ward = ward;
    }

    public UserDTO getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UserDTO createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ShiftDTO)) {
            return false;
        }

        ShiftDTO shiftDTO = (ShiftDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, shiftDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ShiftDTO{" +
            "id=" + getId() +
            ", shiftDate='" + getShiftDate() + "'" +
            ", shiftType='" + getShiftType() + "'" +
            ", startsAt='" + getStartsAt() + "'" +
            ", endsAt='" + getEndsAt() + "'" +
            ", note='" + getNote() + "'" +
            ", staffRecord=" + getStaffRecord() +
            ", ward=" + getWard() +
            ", createdBy=" + getCreatedBy() +
            "}";
    }
}
