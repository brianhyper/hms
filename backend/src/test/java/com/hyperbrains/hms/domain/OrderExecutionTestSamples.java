package com.hyperbrains.hms.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class OrderExecutionTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    public static OrderExecution getOrderExecutionSample1() {
        return new OrderExecution().id(1L).notes("notes1");
    }

    public static OrderExecution getOrderExecutionSample2() {
        return new OrderExecution().id(2L).notes("notes2");
    }

    public static OrderExecution getOrderExecutionRandomSampleGenerator() {
        return new OrderExecution().id(longCount.incrementAndGet()).notes(UUID.randomUUID().toString());
    }
}
