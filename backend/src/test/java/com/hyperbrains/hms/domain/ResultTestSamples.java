package com.hyperbrains.hms.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class ResultTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    public static Result getResultSample1() {
        return new Result().id(1L).imageReference("imageReference1");
    }

    public static Result getResultSample2() {
        return new Result().id(2L).imageReference("imageReference2");
    }

    public static Result getResultRandomSampleGenerator() {
        return new Result().id(longCount.incrementAndGet()).imageReference(UUID.randomUUID().toString());
    }
}
