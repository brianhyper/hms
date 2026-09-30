package com.hyperbrains.hms.service.dto.view;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.Instant;

/**
 * Putting a doctor on duty for a ward.
 *
 * <p>The doctor is named rather than implied by the session: the person who records the roster is an
 * administrator, and the person being rostered is somebody else. Keeping those two apart is the whole
 * point of {@code assignedBy}, which is taken from the session instead.
 */
public class AssignWardCoverRequestDTO implements Serializable {

    @NotNull
    private Long doctorId;

    @NotNull
    private Long wardId;

    /** When the cover starts. Required: an administrator writing the roster knows the shift it is for. */
    @NotNull
    private Instant coversFrom;

    /** When it ends. Absent means open-ended — a rotation with no known end. */
    private Instant coversTo;

    @Size(max = 500)
    private String note;

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public Long getWardId() {
        return wardId;
    }

    public void setWardId(Long wardId) {
        this.wardId = wardId;
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
}
