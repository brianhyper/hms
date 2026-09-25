package com.hyperbrains.hms.service.mapper;

import static com.hyperbrains.hms.domain.AdmissionAsserts.*;
import static com.hyperbrains.hms.domain.AdmissionTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AdmissionMapperTest {

    private AdmissionMapper admissionMapper;

    @BeforeEach
    void setUp() {
        admissionMapper = new AdmissionMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getAdmissionSample1();
        var actual = admissionMapper.toEntity(admissionMapper.toDto(expected));
        assertAdmissionAllPropertiesEquals(expected, actual);
    }
}
