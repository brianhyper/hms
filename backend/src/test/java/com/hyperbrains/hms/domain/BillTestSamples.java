package com.hyperbrains.hms.domain;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class BillTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    public static Bill getBillSample1() {
        return new Bill().id(1L);
    }

    public static Bill getBillSample2() {
        return new Bill().id(2L);
    }

    public static Bill getBillRandomSampleGenerator() {
        return new Bill().id(longCount.incrementAndGet());
    }
}
