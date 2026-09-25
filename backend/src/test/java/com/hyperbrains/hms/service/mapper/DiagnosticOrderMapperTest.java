package com.hyperbrains.hms.service.mapper;

import static com.hyperbrains.hms.domain.DiagnosticOrderAsserts.*;
import static com.hyperbrains.hms.domain.DiagnosticOrderTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DiagnosticOrderMapperTest {

    private DiagnosticOrderMapper diagnosticOrderMapper;

    @BeforeEach
    void setUp() {
        diagnosticOrderMapper = new DiagnosticOrderMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getDiagnosticOrderSample1();
        var actual = diagnosticOrderMapper.toEntity(diagnosticOrderMapper.toDto(expected));
        assertDiagnosticOrderAllPropertiesEquals(expected, actual);
    }
}
