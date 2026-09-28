package com.hyperbrains.hms.service.dto.view;

import com.hyperbrains.hms.domain.enumeration.AdmissionStatus;
import java.time.Instant;

/**
 * What putting a patient into a bed produced.
 *
 * <p>Carries both ends of both changes — the admission's status and the bed's — so the screen can show
 * what happened without having to have captured the previous state itself, and a caller that raced
 * another nurse cannot mistake somebody else's change for its own.
 */
public record AdmissionBedResultDTO(
    Long admissionId,
    AdmissionStatus previousStatus,
    AdmissionStatus status,
    Long bedId,
    String bedNumber,
    Long wardId,
    String wardName,
    Long patientId,
    String patientHospitalId,
    Instant assignedAt
) {}
