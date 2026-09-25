package com.hyperbrains.hms.service.mapper;

import static com.hyperbrains.hms.domain.DrugAsserts.*;
import static com.hyperbrains.hms.domain.DrugTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DrugMapperTest {

    private DrugMapper drugMapper;

    @BeforeEach
    void setUp() {
        drugMapper = new DrugMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getDrugSample1();
        var actual = drugMapper.toEntity(drugMapper.toDto(expected));
        assertDrugAllPropertiesEquals(expected, actual);
    }
}
