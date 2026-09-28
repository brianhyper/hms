package com.hyperbrains.hms.service.dto.view;

import java.time.Instant;

/**
 * What moving a patient to another bed produced.
 *
 * <p>Names both ends, ward included, because the ward is the part that mattered: the move changes which
 * ward's staff are responsible for the patient, and that is not visible from two bed numbers.
 */
public record WardTransferResultDTO(
    Long transferId,
    Long admissionId,
    Long fromBedId,
    String fromBedNumber,
    Long fromWardId,
    String fromWardName,
    Long toBedId,
    String toBedNumber,
    Long toWardId,
    String toWardName,
    String reason,
    Instant transferredAt,
    Long patientId,
    String patientHospitalId
) {}
