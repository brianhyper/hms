package com.hyperbrains.hms.service.mapper;

import static com.hyperbrains.hms.domain.ResultAsserts.*;
import static com.hyperbrains.hms.domain.ResultTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ResultMapperTest {

    private ResultMapper resultMapper;

    @BeforeEach
    void setUp() {
        resultMapper = new ResultMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getResultSample1();
        var actual = resultMapper.toEntity(resultMapper.toDto(expected));
        assertResultAllPropertiesEquals(expected, actual);
    }
}
