package com.hyperbrains.hms.service.dto.view;

import java.time.Instant;
import java.util.List;

/**
 * What a correction actually did.
 *
 * <p>Names the fields that changed rather than just saying "ok": a caller that sent a field the endpoint
 * did not recognise, or a value identical to what was already stored, gets an empty list and can see
 * that nothing happened instead of assuming it did.
 */
public record PatientCorrectionResultDTO(Long patientId, String reason, List<String> changedFields, Instant correctedAt) {}
