package com.hyperbrains.hms.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class InpatientVitalsTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + 2 * Short.MAX_VALUE);

    public static InpatientVitals getInpatientVitalsSample1() {
        return new InpatientVitals().id(1L).pulseRate(1).systolicBp(1).diastolicBp(1).oxygenSaturation(1).notes("notes1");
    }

    public static InpatientVitals getInpatientVitalsSample2() {
        return new InpatientVitals().id(2L).pulseRate(2).systolicBp(2).diastolicBp(2).oxygenSaturation(2).notes("notes2");
    }

    public static InpatientVitals getInpatientVitalsRandomSampleGenerator() {
        return new InpatientVitals()
            .id(longCount.incrementAndGet())
            .pulseRate(intCount.incrementAndGet())
            .systolicBp(intCount.incrementAndGet())
            .diastolicBp(intCount.incrementAndGet())
            .oxygenSaturation(intCount.incrementAndGet())
            .notes(UUID.randomUUID().toString());
    }
}
