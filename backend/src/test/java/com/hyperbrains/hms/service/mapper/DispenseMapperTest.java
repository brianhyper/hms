package com.hyperbrains.hms.service.mapper;

import static com.hyperbrains.hms.domain.DispenseAsserts.*;
import static com.hyperbrains.hms.domain.DispenseTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DispenseMapperTest {

    private DispenseMapper dispenseMapper;

    @BeforeEach
    void setUp() {
        dispenseMapper = new DispenseMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getDispenseSample1();
        var actual = dispenseMapper.toEntity(dispenseMapper.toDto(expected));
        assertDispenseAllPropertiesEquals(expected, actual);
    }
}
