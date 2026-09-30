package com.hyperbrains.hms.service.dto.view;

import com.hyperbrains.hms.domain.enumeration.DoctorOrderRecurrence;
import com.hyperbrains.hms.domain.enumeration.DoctorOrderStatus;
import com.hyperbrains.hms.domain.enumeration.DoctorOrderType;
import java.time.Instant;
import java.util.List;

/**
 * An order as the ward reads it, with everything that has been done about it.
 *
 * <p>The executions travel with the order rather than in a second request, because the question a nurse or a
 * doctor actually asks is "what has been given, and what was written down about it" — and an order sheet that
 * shows the instruction without its history is a sheet that has to be cross-referenced.
 *
 * <p>{@code prescriptionId} is present exactly when the order is a DRUG order, because §6 forbids a drug
 * order without one. It is how the supply side — the reservation, the pharmacy queue, the charge — is reached
 * from the clinical instruction.
 */
public record DoctorOrderViewDTO(
    Long orderId,
    Long admissionId,
    DoctorOrderType type,
    DoctorOrderRecurrence recurrence,
    DoctorOrderStatus status,
    String details,
    String frequency,
    Instant endDate,
    Instant orderedAt,
    String orderedBy,
    Long prescriptionId,
    Instant cancelledAt,
    String cancelledBy,
    String cancelReason,
    List<OrderExecutionViewDTO> executions
) {}
