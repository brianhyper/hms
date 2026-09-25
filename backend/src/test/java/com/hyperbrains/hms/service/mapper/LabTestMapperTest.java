package com.hyperbrains.hms.service.mapper;

import static com.hyperbrains.hms.domain.LabTestAsserts.*;
import static com.hyperbrains.hms.domain.LabTestTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LabTestMapperTest {

    private LabTestMapper labTestMapper;

    @BeforeEach
    void setUp() {
        labTestMapper = new LabTestMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getLabTestSample1();
        var actual = labTestMapper.toEntity(labTestMapper.toDto(expected));
        assertLabTestAllPropertiesEquals(expected, actual);
    }
}
