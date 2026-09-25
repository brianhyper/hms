package com.hyperbrains.hms.service.dto.view;

import com.hyperbrains.hms.domain.enumeration.ReferralType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * Referring a patient on.
 *
 * <p>The destination is free text because the receiving facility may be anywhere — another hospital,
 * a clinic, a specialist practice — and forcing it to be one of our own records would make an external
 * referral impossible.
 */
public class CreateReferralRequestDTO implements Serializable {

    /** Whether the destination is inside this hospital or outside it. */
    @NotNull
    private ReferralType type;

    /** Where the patient is being sent, as it should appear on the letter. */
    @NotBlank
    @Size(max = 255)
    private String destination;

    /** Where the letter is emailed. Optional: it may be printed and handed to the patient instead. */
    @Email
    @Size(max = 254)
    private String destinationEmail;

    /** An internal destination, so the letter can name the department it is going to. */
    private Long departmentId;

    /** Why the patient is being referred. This is the clinical content of the letter. */
    @NotBlank
    @Size(max = 10000)
    private String reason;

    /** Anything the receiving clinician should know that does not fit the reason. */
    @Size(max = 10000)
    private String notes;

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

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
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
}
