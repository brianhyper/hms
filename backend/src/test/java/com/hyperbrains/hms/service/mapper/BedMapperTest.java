package com.hyperbrains.hms.service.mapper;

import static com.hyperbrains.hms.domain.BedAsserts.*;
import static com.hyperbrains.hms.domain.BedTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BedMapperTest {

    private BedMapper bedMapper;

    @BeforeEach
    void setUp() {
        bedMapper = new BedMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getBedSample1();
        var actual = bedMapper.toEntity(bedMapper.toDto(expected));
        assertBedAllPropertiesEquals(expected, actual);
    }
}
