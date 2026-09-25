package com.hyperbrains.hms.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class AdmissionTransferTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    public static AdmissionTransfer getAdmissionTransferSample1() {
        return new AdmissionTransfer().id(1L).reason("reason1");
    }

    public static AdmissionTransfer getAdmissionTransferSample2() {
        return new AdmissionTransfer().id(2L).reason("reason2");
    }

    public static AdmissionTransfer getAdmissionTransferRandomSampleGenerator() {
        return new AdmissionTransfer().id(longCount.incrementAndGet()).reason(UUID.randomUUID().toString());
    }
}
