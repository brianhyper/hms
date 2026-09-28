package com.hyperbrains.hms.service.rules;

import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.domain.Bed;
import com.hyperbrains.hms.domain.BedType;
import com.hyperbrains.hms.domain.enumeration.BedStatus;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/** Pure rules for what a night in a bed costs. No database involved. */
class BedPricingTest {

    private static final BigDecimal TYPE_RATE = new BigDecimal("3500.00");

    private static final BigDecimal BED_RATE = new BigDecimal("4200.00");

    @Test
    void aBedWithoutItsOwnRateIsPricedByItsType() {
        Bed bed = bed(null, TYPE_RATE);

        assertThat(BedPricing.effectiveDailyRate(bed)).isEqualByComparingTo(TYPE_RATE);
        assertThat(BedPricing.rateComesFromTheBed(bed)).isFalse();
    }

    @Test
    void aBedWithItsOwnRateIgnoresItsType() {
        Bed bed = bed(BED_RATE, TYPE_RATE);

        assertThat(BedPricing.effectiveDailyRate(bed)).isEqualByComparingTo(BED_RATE);
        assertThat(BedPricing.rateComesFromTheBed(bed)).isTrue();
    }

    /**
     * Zero is a price, not a missing value. Reading it as unset would silently re-price a deliberately
     * free bed at its type's rate — the opposite of what was intended, and invisible on the bill.
     */
    @Test
    void aZeroRateIsAPriceAndNotAGap() {
        Bed bed = bed(BigDecimal.ZERO, TYPE_RATE);

        assertThat(BedPricing.effectiveDailyRate(bed)).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(BedPricing.rateComesFromTheBed(bed)).isTrue();
    }

    /** Nothing at all is reported as nothing, rather than as a free bed. */
    @Test
    void aBedWithNeitherRateResolvesToNothing() {
        assertThat(BedPricing.effectiveDailyRate(bed(null, null))).isNull();
        assertThat(BedPricing.effectiveDailyRate(bed(null, null).bedType(null))).isNull();
        assertThat(BedPricing.effectiveDailyRate(null)).isNull();
    }

    @Test
    void aBedWithNoTypeAtAllStillUsesItsOwnRate() {
        Bed bed = bed(BED_RATE, TYPE_RATE).bedType(null);

        assertThat(BedPricing.effectiveDailyRate(bed)).isEqualByComparingTo(BED_RATE);
        assertThat(BedPricing.rateComesFromTheBed(bed)).isTrue();
    }

    private static Bed bed(BigDecimal bedRate, BigDecimal typeRate) {
        Bed bed = new Bed();
        bed.setBedNumber("A-01");
        bed.setStatus(BedStatus.AVAILABLE);
        bed.setDailyRateOverride(bedRate);
        if (typeRate != null) {
            BedType type = new BedType();
            type.setName("GENERAL");
            type.setDefaultDailyRate(typeRate);
            type.setActive(true);
            bed.setBedType(type);
        }
        return bed;
    }
}
