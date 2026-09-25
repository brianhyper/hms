package com.hyperbrains.hms.service.mapper;

import static com.hyperbrains.hms.domain.DiagnosisAsserts.*;
import static com.hyperbrains.hms.domain.DiagnosisTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DiagnosisMapperTest {

    private DiagnosisMapper diagnosisMapper;

    @BeforeEach
    void setUp() {
        diagnosisMapper = new DiagnosisMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getDiagnosisSample1();
        var actual = diagnosisMapper.toEntity(diagnosisMapper.toDto(expected));
        assertDiagnosisAllPropertiesEquals(expected, actual);
    }
}
