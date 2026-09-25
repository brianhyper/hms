package com.hyperbrains.hms.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class PrescriptionTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    public static Prescription getPrescriptionSample1() {
        return new Prescription().id(1L).prescribingSource("prescribingSource1");
    }

    public static Prescription getPrescriptionSample2() {
        return new Prescription().id(2L).prescribingSource("prescribingSource2");
    }

    public static Prescription getPrescriptionRandomSampleGenerator() {
        return new Prescription().id(longCount.incrementAndGet()).prescribingSource(UUID.randomUUID().toString());
    }
}
