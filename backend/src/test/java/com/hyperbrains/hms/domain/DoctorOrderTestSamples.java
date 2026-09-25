package com.hyperbrains.hms.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class DoctorOrderTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    public static DoctorOrder getDoctorOrderSample1() {
        return new DoctorOrder().id(1L).frequency("frequency1").details("details1").cancelReason("cancelReason1");
    }

    public static DoctorOrder getDoctorOrderSample2() {
        return new DoctorOrder().id(2L).frequency("frequency2").details("details2").cancelReason("cancelReason2");
    }

    public static DoctorOrder getDoctorOrderRandomSampleGenerator() {
        return new DoctorOrder()
            .id(longCount.incrementAndGet())
            .frequency(UUID.randomUUID().toString())
            .details(UUID.randomUUID().toString())
            .cancelReason(UUID.randomUUID().toString());
    }
}
