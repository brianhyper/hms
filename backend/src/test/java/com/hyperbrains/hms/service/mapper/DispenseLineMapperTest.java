package com.hyperbrains.hms.service.mapper;

import static com.hyperbrains.hms.domain.DispenseLineAsserts.*;
import static com.hyperbrains.hms.domain.DispenseLineTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DispenseLineMapperTest {

    private DispenseLineMapper dispenseLineMapper;

    @BeforeEach
    void setUp() {
        dispenseLineMapper = new DispenseLineMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getDispenseLineSample1();
        var actual = dispenseLineMapper.toEntity(dispenseLineMapper.toDto(expected));
        assertDispenseLineAllPropertiesEquals(expected, actual);
    }
}
