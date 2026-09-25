package com.hyperbrains.hms.service.mapper;

import static com.hyperbrains.hms.domain.RadiologyExamAsserts.*;
import static com.hyperbrains.hms.domain.RadiologyExamTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RadiologyExamMapperTest {

    private RadiologyExamMapper radiologyExamMapper;

    @BeforeEach
    void setUp() {
        radiologyExamMapper = new RadiologyExamMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getRadiologyExamSample1();
        var actual = radiologyExamMapper.toEntity(radiologyExamMapper.toDto(expected));
        assertRadiologyExamAllPropertiesEquals(expected, actual);
    }
}
