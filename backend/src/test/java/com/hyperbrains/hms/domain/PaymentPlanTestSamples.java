package com.hyperbrains.hms.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class PaymentPlanTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    public static PaymentPlan getPaymentPlanSample1() {
        return new PaymentPlan()
            .id(1L)
            .guarantorName("guarantorName1")
            .guarantorRelationship("guarantorRelationship1")
            .guarantorPhone("guarantorPhone1")
            .notes("notes1");
    }

    public static PaymentPlan getPaymentPlanSample2() {
        return new PaymentPlan()
            .id(2L)
            .guarantorName("guarantorName2")
            .guarantorRelationship("guarantorRelationship2")
            .guarantorPhone("guarantorPhone2")
            .notes("notes2");
    }

    public static PaymentPlan getPaymentPlanRandomSampleGenerator() {
        return new PaymentPlan()
            .id(longCount.incrementAndGet())
            .guarantorName(UUID.randomUUID().toString())
            .guarantorRelationship(UUID.randomUUID().toString())
            .guarantorPhone(UUID.randomUUID().toString())
            .notes(UUID.randomUUID().toString());
    }
}
