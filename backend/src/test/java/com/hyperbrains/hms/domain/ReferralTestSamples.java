package com.hyperbrains.hms.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class ReferralTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);

    public static Referral getReferralSample1() {
        return new Referral().id(1L).destination("destination1").destinationEmail("destinationEmail1");
    }

    public static Referral getReferralSample2() {
        return new Referral().id(2L).destination("destination2").destinationEmail("destinationEmail2");
    }

    public static Referral getReferralRandomSampleGenerator() {
        return new Referral()
            .id(longCount.incrementAndGet())
            .destination(UUID.randomUUID().toString())
            .destinationEmail(UUID.randomUUID().toString());
    }
}
