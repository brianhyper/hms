package com.hyperbrains.hms.service.dto.view;

import java.time.Instant;

/**
 * One occasion on which an order was carried out.
 */
public record OrderExecutionViewDTO(Long id, Instant executedAt, String executedBy, String notes) {}
