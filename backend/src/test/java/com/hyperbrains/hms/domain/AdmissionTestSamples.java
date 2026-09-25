package com.hyperbrains.hms.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class AdmissionTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    public static Admission getAdmissionSample1() {
        return new Admission().id(1L).admissionReason("admissionReason1").dischargeNote("dischargeNote1");
    }

    public static Admission getAdmissionSample2() {
        return new Admission().id(2L).admissionReason("admissionReason2").dischargeNote("dischargeNote2");
    }

    public static Admission getAdmissionRandomSampleGenerator() {
        return new Admission()
            .id(longCount.incrementAndGet())
            .admissionReason(UUID.randomUUID().toString())
            .dischargeNote(UUID.randomUUID().toString());
    }
}
