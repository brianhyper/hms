package com.hyperbrains.hms.domain;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class DispenseLineTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + 2 * Short.MAX_VALUE);

    public static DispenseLine getDispenseLineSample1() {
        return new DispenseLine().id(1L).quantity(1);
    }

    public static DispenseLine getDispenseLineSample2() {
        return new DispenseLine().id(2L).quantity(2);
    }

    public static DispenseLine getDispenseLineRandomSampleGenerator() {
        return new DispenseLine().id(longCount.incrementAndGet()).quantity(intCount.incrementAndGet());
    }
}
