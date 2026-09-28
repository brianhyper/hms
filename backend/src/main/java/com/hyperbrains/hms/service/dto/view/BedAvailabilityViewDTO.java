package com.hyperbrains.hms.service.dto.view;

import java.math.BigDecimal;

/**
 * A bed that can be given to a patient right now.
 *
 * <p>Only beds the assignment rules would accept are ever in this list, so the ward, the bed type and
 * the rate are here to let whoever is choosing compare beds — not for the client to re-apply the
 * availability rule and disagree with the server about it.
 *
 * <p>The rate is carried with {@code rateComesFromTheBed} because a resolved rate is indistinguishable
 * from an overridden one, and "why is this ICU bed cheaper than the others" is otherwise unanswerable
 * from the screen.
 */
public record BedAvailabilityViewDTO(
    Long bedId,
    String bedNumber,
    Long wardId,
    String wardName,
    Long bedTypeId,
    String bedTypeName,
    BigDecimal dailyRate,
    boolean rateComesFromTheBed
) {}
