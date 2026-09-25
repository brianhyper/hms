package com.hyperbrains.hms.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class VitalSignsTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + 2 * Short.MAX_VALUE);

    public static VitalSigns getVitalSignsSample1() {
        return new VitalSigns()
            .id(1L)
            .pulseRate(1)
            .systolicBp(1)
            .diastolicBp(1)
            .oxygenSaturation(1)
            .nutritionalStatus("nutritionalStatus1");
    }

    public static VitalSigns getVitalSignsSample2() {
        return new VitalSigns()
            .id(2L)
            .pulseRate(2)
            .systolicBp(2)
            .diastolicBp(2)
            .oxygenSaturation(2)
            .nutritionalStatus("nutritionalStatus2");
    }

    public static VitalSigns getVitalSignsRandomSampleGenerator() {
        return new VitalSigns()
            .id(longCount.incrementAndGet())
            .pulseRate(intCount.incrementAndGet())
            .systolicBp(intCount.incrementAndGet())
            .diastolicBp(intCount.incrementAndGet())
            .oxygenSaturation(intCount.incrementAndGet())
            .nutritionalStatus(UUID.randomUUID().toString());
    }
}
