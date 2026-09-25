package com.hyperbrains.hms.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class AdHocChargeTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    public static AdHocCharge getAdHocChargeSample1() {
        return new AdHocCharge().id(1L).description("description1").reason("reason1").voidReason("voidReason1");
    }

    public static AdHocCharge getAdHocChargeSample2() {
        return new AdHocCharge().id(2L).description("description2").reason("reason2").voidReason("voidReason2");
    }

    public static AdHocCharge getAdHocChargeRandomSampleGenerator() {
        return new AdHocCharge()
            .id(longCount.incrementAndGet())
            .description(UUID.randomUUID().toString())
            .reason(UUID.randomUUID().toString())
            .voidReason(UUID.randomUUID().toString());
    }
}
