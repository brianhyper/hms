package com.hyperbrains.hms.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class PatientTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + 2L * Integer.MAX_VALUE);
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + 2 * Short.MAX_VALUE);

    public static Patient getPatientSample1() {
        return new Patient()
            .id(1L)
            .hospitalId("hospitalId1")
            .fullName("fullName1")
            .estimatedAge(1)
            .phone("phone1")
            .email("email1")
            .identityDocumentNumber("identityDocumentNumber1")
            .occupation("occupation1")
            .maritalStatus("maritalStatus1")
            .nextOfKinName("nextOfKinName1")
            .nextOfKinPhone("nextOfKinPhone1")
            .villageEstate("villageEstate1");
    }

    public static Patient getPatientSample2() {
        return new Patient()
            .id(2L)
            .hospitalId("hospitalId2")
            .fullName("fullName2")
            .estimatedAge(2)
            .phone("phone2")
            .email("email2")
            .identityDocumentNumber("identityDocumentNumber2")
            .occupation("occupation2")
            .maritalStatus("maritalStatus2")
            .nextOfKinName("nextOfKinName2")
            .nextOfKinPhone("nextOfKinPhone2")
            .villageEstate("villageEstate2");
    }

    public static Patient getPatientRandomSampleGenerator() {
        return new Patient()
            .id(longCount.incrementAndGet())
            .hospitalId(UUID.randomUUID().toString())
            .fullName(UUID.randomUUID().toString())
            .estimatedAge(intCount.incrementAndGet())
            .phone(UUID.randomUUID().toString())
            .email(UUID.randomUUID().toString())
            .identityDocumentNumber(UUID.randomUUID().toString())
            .occupation(UUID.randomUUID().toString())
            .maritalStatus(UUID.randomUUID().toString())
            .nextOfKinName(UUID.randomUUID().toString())
            .nextOfKinPhone(UUID.randomUUID().toString())
            .villageEstate(UUID.randomUUID().toString());
    }
}
