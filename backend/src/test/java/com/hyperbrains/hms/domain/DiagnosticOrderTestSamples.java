package com.hyperbrains.hms.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class DiagnosticOrderTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    public static DiagnosticOrder getDiagnosticOrderSample1() {
        return new DiagnosticOrder().id(1L).testName("testName1");
    }

    public static DiagnosticOrder getDiagnosticOrderSample2() {
        return new DiagnosticOrder().id(2L).testName("testName2");
    }

    public static DiagnosticOrder getDiagnosticOrderRandomSampleGenerator() {
        return new DiagnosticOrder().id(longCount.incrementAndGet()).testName(UUID.randomUUID().toString());
    }
}
