package com.hyperbrains.hms.service.mapper;

import static com.hyperbrains.hms.domain.BedTypeAsserts.*;
import static com.hyperbrains.hms.domain.BedTypeTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BedTypeMapperTest {

    private BedTypeMapper bedTypeMapper;

    @BeforeEach
    void setUp() {
        bedTypeMapper = new BedTypeMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getBedTypeSample1();
        var actual = bedTypeMapper.toEntity(bedTypeMapper.toDto(expected));
        assertBedTypeAllPropertiesEquals(expected, actual);
    }
}
