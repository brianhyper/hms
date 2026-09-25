package com.hyperbrains.hms.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class AuditLogTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    public static AuditLog getAuditLogSample1() {
        return new AuditLog().id(1L).action("action1").entityName("entityName1").entityId("entityId1");
    }

    public static AuditLog getAuditLogSample2() {
        return new AuditLog().id(2L).action("action2").entityName("entityName2").entityId("entityId2");
    }

    public static AuditLog getAuditLogRandomSampleGenerator() {
        return new AuditLog()
            .id(longCount.incrementAndGet())
            .action(UUID.randomUUID().toString())
            .entityName(UUID.randomUUID().toString())
            .entityId(UUID.randomUUID().toString());
    }
}
