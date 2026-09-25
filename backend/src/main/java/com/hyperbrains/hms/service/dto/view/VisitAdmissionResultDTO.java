package com.hyperbrains.hms.service.dto.view;

import com.hyperbrains.hms.domain.enumeration.RegistrationStatus;
import com.hyperbrains.hms.domain.enumeration.VisitStatus;
import com.hyperbrains.hms.domain.enumeration.VisitType;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * What the admission conversion produced, and what the inpatient side inherits with it.
 *
 * <p>The counts are reported rather than acted on, deliberately. Admission leaves the outpatient path
 * for good, which means anything still outstanding at that moment will never resolve through it: an
 * open order keeps being worked and charged, but a prescription awaiting payment can no longer be paid
 * and therefore can no longer be dispensed. The doctor doing the conversion is the only person who can
 * decide whether the ward will supply that medicine instead, so the numbers are put in front of them
 * instead of a policy being invented here.
 *
 * <p>{@code registrationStatus} is reported for the same reason: admitting an unidentified patient is
 * legitimate clinical practice, but the record is not yet a confirmed identity, and that is worth
 * seeing at the moment it happens.
 */
public record VisitAdmissionResultDTO(
    Long visitId,
    VisitType visitType,
    VisitStatus visitStatus,
    Long patientId,
    String patientHospitalId,
    RegistrationStatus registrationStatus,
    Long consultationId,
    int ordersStillOpen,
    int prescriptionsAwaitingDispense,
    BigDecimal chargesAccumulated,
    String admissionReason,
    Instant admittedAt
) {}
