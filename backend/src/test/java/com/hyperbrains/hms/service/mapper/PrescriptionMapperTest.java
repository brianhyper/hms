package com.hyperbrains.hms.service.mapper;

import static com.hyperbrains.hms.domain.PrescriptionAsserts.*;
import static com.hyperbrains.hms.domain.PrescriptionTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PrescriptionMapperTest {

    private PrescriptionMapper prescriptionMapper;

    @BeforeEach
    void setUp() {
        prescriptionMapper = new PrescriptionMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getPrescriptionSample1();
        var actual = prescriptionMapper.toEntity(prescriptionMapper.toDto(expected));
        assertPrescriptionAllPropertiesEquals(expected, actual);
    }
}
