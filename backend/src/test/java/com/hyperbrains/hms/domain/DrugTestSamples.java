package com.hyperbrains.hms.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class DrugTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + 2 * Short.MAX_VALUE);

    public static Drug getDrugSample1() {
        return new Drug().id(1L).name("name1").unit("unit1").currentStock(1).reservedStock(1).lowStockThreshold(1);
    }

    public static Drug getDrugSample2() {
        return new Drug().id(2L).name("name2").unit("unit2").currentStock(2).reservedStock(2).lowStockThreshold(2);
    }

    public static Drug getDrugRandomSampleGenerator() {
        return new Drug()
            .id(longCount.incrementAndGet())
            .name(UUID.randomUUID().toString())
            .unit(UUID.randomUUID().toString())
            .currentStock(intCount.incrementAndGet())
            .reservedStock(intCount.incrementAndGet())
            .lowStockThreshold(intCount.incrementAndGet());
    }
}
