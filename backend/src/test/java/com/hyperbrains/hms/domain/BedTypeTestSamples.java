package com.hyperbrains.hms.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class BedTypeTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    public static BedType getBedTypeSample1() {
        return new BedType().id(1L).name("name1");
    }

    public static BedType getBedTypeSample2() {
        return new BedType().id(2L).name("name2");
    }

    public static BedType getBedTypeRandomSampleGenerator() {
        return new BedType().id(longCount.incrementAndGet()).name(UUID.randomUUID().toString());
    }
}
