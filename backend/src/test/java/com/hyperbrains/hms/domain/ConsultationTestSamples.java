package com.hyperbrains.hms.domain;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class ConsultationTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    public static Consultation getConsultationSample1() {
        return new Consultation().id(1L);
    }

    public static Consultation getConsultationSample2() {
        return new Consultation().id(2L);
    }

    public static Consultation getConsultationRandomSampleGenerator() {
        return new Consultation().id(longCount.incrementAndGet());
    }
}
