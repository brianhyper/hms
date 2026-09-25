package com.hyperbrains.hms.service.mapper;

import static com.hyperbrains.hms.domain.ConsultationAsserts.*;
import static com.hyperbrains.hms.domain.ConsultationTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConsultationMapperTest {

    private ConsultationMapper consultationMapper;

    @BeforeEach
    void setUp() {
        consultationMapper = new ConsultationMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getConsultationSample1();
        var actual = consultationMapper.toEntity(consultationMapper.toDto(expected));
        assertConsultationAllPropertiesEquals(expected, actual);
    }
}
