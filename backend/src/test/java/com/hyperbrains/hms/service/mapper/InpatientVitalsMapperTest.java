package com.hyperbrains.hms.service.mapper;

import static com.hyperbrains.hms.domain.InpatientVitalsAsserts.*;
import static com.hyperbrains.hms.domain.InpatientVitalsTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InpatientVitalsMapperTest {

    private InpatientVitalsMapper inpatientVitalsMapper;

    @BeforeEach
    void setUp() {
        inpatientVitalsMapper = new InpatientVitalsMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getInpatientVitalsSample1();
        var actual = inpatientVitalsMapper.toEntity(inpatientVitalsMapper.toDto(expected));
        assertInpatientVitalsAllPropertiesEquals(expected, actual);
    }
}
