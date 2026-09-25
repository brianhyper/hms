package com.hyperbrains.hms.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class LabTestTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + 2 * Short.MAX_VALUE);

    public static LabTest getLabTestSample1() {
        return new LabTest().id(1L).name("name1").specimenType("specimenType1").turnaroundTimeMinutes(1);
    }

    public static LabTest getLabTestSample2() {
        return new LabTest().id(2L).name("name2").specimenType("specimenType2").turnaroundTimeMinutes(2);
    }

    public static LabTest getLabTestRandomSampleGenerator() {
        return new LabTest()
            .id(longCount.incrementAndGet())
            .name(UUID.randomUUID().toString())
            .specimenType(UUID.randomUUID().toString())
            .turnaroundTimeMinutes(intCount.incrementAndGet());
    }
}
