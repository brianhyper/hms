package com.hyperbrains.hms.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class BedTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    public static Bed getBedSample1() {
        return new Bed().id(1L).bedNumber("bedNumber1");
    }

    public static Bed getBedSample2() {
        return new Bed().id(2L).bedNumber("bedNumber2");
    }

    public static Bed getBedRandomSampleGenerator() {
        return new Bed().id(longCount.incrementAndGet()).bedNumber(UUID.randomUUID().toString());
    }
}
