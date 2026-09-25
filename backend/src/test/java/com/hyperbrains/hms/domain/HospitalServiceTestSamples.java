package com.hyperbrains.hms.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class HospitalServiceTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    public static HospitalService getHospitalServiceSample1() {
        return new HospitalService().id(1L).name("name1").serviceType("serviceType1");
    }

    public static HospitalService getHospitalServiceSample2() {
        return new HospitalService().id(2L).name("name2").serviceType("serviceType2");
    }

    public static HospitalService getHospitalServiceRandomSampleGenerator() {
        return new HospitalService()
            .id(longCount.incrementAndGet())
            .name(UUID.randomUUID().toString())
            .serviceType(UUID.randomUUID().toString());
    }
}
