package com.hyperbrains.hms.service.mapper;

import static com.hyperbrains.hms.domain.VitalSignsAsserts.*;
import static com.hyperbrains.hms.domain.VitalSignsTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class VitalSignsMapperTest {

    private VitalSignsMapper vitalSignsMapper;

    @BeforeEach
    void setUp() {
        vitalSignsMapper = new VitalSignsMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getVitalSignsSample1();
        var actual = vitalSignsMapper.toEntity(vitalSignsMapper.toDto(expected));
        assertVitalSignsAllPropertiesEquals(expected, actual);
    }
}
