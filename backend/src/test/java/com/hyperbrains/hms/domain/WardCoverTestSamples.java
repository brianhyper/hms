package com.hyperbrains.hms.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class WardCoverTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    public static WardCover getWardCoverSample1() {
        return new WardCover().id(1L).note("note1");
    }

    public static WardCover getWardCoverSample2() {
        return new WardCover().id(2L).note("note2");
    }

    public static WardCover getWardCoverRandomSampleGenerator() {
        return new WardCover().id(longCount.incrementAndGet()).note(UUID.randomUUID().toString());
    }
}
