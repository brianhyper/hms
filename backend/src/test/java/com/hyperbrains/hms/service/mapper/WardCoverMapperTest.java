package com.hyperbrains.hms.service.mapper;

import static com.hyperbrains.hms.domain.WardCoverAsserts.*;
import static com.hyperbrains.hms.domain.WardCoverTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WardCoverMapperTest {

    private WardCoverMapper wardCoverMapper;

    @BeforeEach
    void setUp() {
        wardCoverMapper = new WardCoverMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getWardCoverSample1();
        var actual = wardCoverMapper.toEntity(wardCoverMapper.toDto(expected));
        assertWardCoverAllPropertiesEquals(expected, actual);
    }
}
