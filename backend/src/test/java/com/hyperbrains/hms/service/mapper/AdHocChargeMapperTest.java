package com.hyperbrains.hms.service.mapper;

import static com.hyperbrains.hms.domain.AdHocChargeAsserts.*;
import static com.hyperbrains.hms.domain.AdHocChargeTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AdHocChargeMapperTest {

    private AdHocChargeMapper adHocChargeMapper;

    @BeforeEach
    void setUp() {
        adHocChargeMapper = new AdHocChargeMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getAdHocChargeSample1();
        var actual = adHocChargeMapper.toEntity(adHocChargeMapper.toDto(expected));
        assertAdHocChargeAllPropertiesEquals(expected, actual);
    }
}
