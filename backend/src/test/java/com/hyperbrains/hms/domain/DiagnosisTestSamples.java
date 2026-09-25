package com.hyperbrains.hms.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class DiagnosisTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    public static Diagnosis getDiagnosisSample1() {
        return new Diagnosis().id(1L).code("code1").name("name1");
    }

    public static Diagnosis getDiagnosisSample2() {
        return new Diagnosis().id(2L).code("code2").name("name2");
    }

    public static Diagnosis getDiagnosisRandomSampleGenerator() {
        return new Diagnosis().id(longCount.incrementAndGet()).code(UUID.randomUUID().toString()).name(UUID.randomUUID().toString());
    }
}
