package com.hyperbrains.hms.service.rules;

import com.hyperbrains.hms.domain.Bed;
import com.hyperbrains.hms.domain.BedType;
import java.math.BigDecimal;

/**
 * What a night in a bed costs.
 *
 * <p>Two places hold a rate on purpose. The bed type carries the ordinary price of that kind of bed,
 * so a hospital-wide price change is one edit; the bed carries an optional override, so a premium
 * room or a negotiated rate does not force every other bed of that type to be re-priced. Reading the
 * two in one place keeps the rule — override wins — from being re-implemented slightly differently at
 * each call site, which is how a quoted price and a charged price drift apart.
 *
 * <p>Pure, so the fallback can be tested without a database. The charge itself is raised in the
 * stay-billing slice; this only answers what the rate would be.
 */
public final class BedPricing {

    private BedPricing() {}

    /**
     * The rate that applies to one night in this bed.
     *
     * <p>The bed's own rate wins when it has one; otherwise the rate for its type. A zero is
     * deliberately <em>not</em> treated as "unset" — a free bed has to be distinguishable from a bed
     * whose price was never entered, and treating zero as unset would make the second look free too.
     * Returns null only when neither rate can be read at all.
     */
    public static BigDecimal effectiveDailyRate(Bed bed) {
        if (bed == null) {
            return null;
        }
        BigDecimal override = bed.getDailyRateOverride();
        if (override != null) {
            return override;
        }
        BedType type = bed.getBedType();
        return type == null ? null : type.getDefaultDailyRate();
    }

    /**
     * Whether that rate came from the bed rather than from its type.
     *
     * <p>Reported alongside the rate because the two look identical once resolved, and "why is this
     * ICU bed cheaper than the others" is otherwise unanswerable from the screen.
     */
    public static boolean rateComesFromTheBed(Bed bed) {
        return bed != null && bed.getDailyRateOverride() != null;
    }
}
