package com.hyperbrains.hms.service.dto.view;

import com.hyperbrains.hms.domain.enumeration.Sex;
import java.time.Instant;
import java.time.LocalDate;

/**
 * One patient waiting for a bed.
 *
 * <p>A projection rather than the {@code Admission} entity: the person choosing a bed needs to recognise
 * the patient, and the entity's relations are mapped as identifiers only, which would force the client
 * into a second request per row.
 *
 * <p>The beds themselves are not in this list. They come from the availability query, which answers
 * "where can this patient go" from the bed's own state; duplicating that here would create a second
 * answer to the same question that could disagree with the first.
 */
public record AwaitingBedViewDTO(
    Long admissionId,
    Long visitId,
    Long patientId,
    String patientHospitalId,
    String patientName,
    Sex patientSex,
    LocalDate patientDateOfBirth,
    Integer patientEstimatedAge,
    Instant admittedAt,
    String admissionReason,
    String admittingDoctor,
    String primaryDoctor
) {}
