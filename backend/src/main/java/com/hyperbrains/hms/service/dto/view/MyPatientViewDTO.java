package com.hyperbrains.hms.service.dto.view;

import com.hyperbrains.hms.domain.enumeration.AdmissionStatus;
import com.hyperbrains.hms.domain.enumeration.Sex;
import java.time.Instant;

/**
 * One inpatient, as seen on a doctor's own list.
 *
 * <p>{@code seenBecause} is here on purpose. A doctor's list mixes two populations — patients they are
 * responsible for and patients on a ward they happen to be covering — and the two carry different
 * obligations. Showing them as one undifferentiated list is how somebody assumes they are the doctor
 * responsible for a patient they are merely covering for.
 */
public record MyPatientViewDTO(
    Long admissionId,
    Long visitId,
    Long patientId,
    String patientHospitalId,
    String patientName,
    Sex patientSex,
    AdmissionStatus status,
    Long bedId,
    String bedNumber,
    Long wardId,
    String wardName,
    Instant admittedAt,
    String primaryDoctor,
    SeenBecause seenBecause
) {
    /** Why this patient is on the list at all. */
    public enum SeenBecause {
        /** The caller is the patient's responsible doctor. */
        PRIMARY_DOCTOR,
        /** The caller is covering the ward the patient is currently in. */
        COVERING_WARD,
        /** The caller's role sees every inpatient — a nurse, an administrator or the super-admin. */
        FULL_ACCESS,
    }
}
