package com.hyperbrains.hms.domain;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class DispenseTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    public static Dispense getDispenseSample1() {
        return new Dispense().id(1L);
    }

    public static Dispense getDispenseSample2() {
        return new Dispense().id(2L);
    }

    public static Dispense getDispenseRandomSampleGenerator() {
        return new Dispense().id(longCount.incrementAndGet());
    }
}
