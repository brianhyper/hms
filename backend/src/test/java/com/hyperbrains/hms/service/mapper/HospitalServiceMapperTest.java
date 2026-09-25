package com.hyperbrains.hms.service.mapper;

import static com.hyperbrains.hms.domain.HospitalServiceAsserts.*;
import static com.hyperbrains.hms.domain.HospitalServiceTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HospitalServiceMapperTest {

    private HospitalServiceMapper hospitalServiceMapper;

    @BeforeEach
    void setUp() {
        hospitalServiceMapper = new HospitalServiceMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getHospitalServiceSample1();
        var actual = hospitalServiceMapper.toEntity(hospitalServiceMapper.toDto(expected));
        assertHospitalServiceAllPropertiesEquals(expected, actual);
    }
}
