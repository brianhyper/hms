package com.hyperbrains.hms.service.mapper;

import static com.hyperbrains.hms.domain.PrescriptionLineAsserts.*;
import static com.hyperbrains.hms.domain.PrescriptionLineTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PrescriptionLineMapperTest {

    private PrescriptionLineMapper prescriptionLineMapper;

    @BeforeEach
    void setUp() {
        prescriptionLineMapper = new PrescriptionLineMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getPrescriptionLineSample1();
        var actual = prescriptionLineMapper.toEntity(prescriptionLineMapper.toDto(expected));
        assertPrescriptionLineAllPropertiesEquals(expected, actual);
    }
}
