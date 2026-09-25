package com.hyperbrains.hms.service.dto.view;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * A doctor deciding that this patient is staying.
 *
 * <p>The reason is mandatory because the admission changes what the hospital owes the patient for the
 * rest of their stay, and because the visit's own {@code reasonForVisit} describes why they walked in,
 * which is frequently not why they are being kept in.
 */
public class AdmitPatientRequestDTO implements Serializable {

    /** Why the patient is being admitted. This is the clinical ground for the stay. */
    @NotBlank
    @Size(max = 10000)
    private String admissionReason;

    public String getAdmissionReason() {
        return admissionReason;
    }

    public void setAdmissionReason(String admissionReason) {
        this.admissionReason = admissionReason;
    }
}
