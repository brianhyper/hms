package com.hyperbrains.hms.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class BillLineItemTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    public static BillLineItem getBillLineItemSample1() {
        return new BillLineItem().id(1L).description("description1");
    }

    public static BillLineItem getBillLineItemSample2() {
        return new BillLineItem().id(2L).description("description2");
    }

    public static BillLineItem getBillLineItemRandomSampleGenerator() {
        return new BillLineItem().id(longCount.incrementAndGet()).description(UUID.randomUUID().toString());
    }
}
