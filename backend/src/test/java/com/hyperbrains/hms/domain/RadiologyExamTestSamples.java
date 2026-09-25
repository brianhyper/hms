package com.hyperbrains.hms.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class RadiologyExamTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    public static RadiologyExam getRadiologyExamSample1() {
        return new RadiologyExam().id(1L).name("name1");
    }

    public static RadiologyExam getRadiologyExamSample2() {
        return new RadiologyExam().id(2L).name("name2");
    }

    public static RadiologyExam getRadiologyExamRandomSampleGenerator() {
        return new RadiologyExam().id(longCount.incrementAndGet()).name(UUID.randomUUID().toString());
    }
}
