package com.hyperbrains.hms.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class PrescriptionLineTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + 2 * Short.MAX_VALUE);

    public static PrescriptionLine getPrescriptionLineSample1() {
        return new PrescriptionLine().id(1L).dosage("dosage1").duration("duration1").quantity(1);
    }

    public static PrescriptionLine getPrescriptionLineSample2() {
        return new PrescriptionLine().id(2L).dosage("dosage2").duration("duration2").quantity(2);
    }

    public static PrescriptionLine getPrescriptionLineRandomSampleGenerator() {
        return new PrescriptionLine()
            .id(longCount.incrementAndGet())
            .dosage(UUID.randomUUID().toString())
            .duration(UUID.randomUUID().toString())
            .quantity(intCount.incrementAndGet());
    }
}
