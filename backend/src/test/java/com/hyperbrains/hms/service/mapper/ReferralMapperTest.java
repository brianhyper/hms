package com.hyperbrains.hms.service.mapper;

import static com.hyperbrains.hms.domain.ReferralAsserts.*;
import static com.hyperbrains.hms.domain.ReferralTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReferralMapperTest {

    private ReferralMapper referralMapper;

    @BeforeEach
    void setUp() {
        referralMapper = new ReferralMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getReferralSample1();
        var actual = referralMapper.toEntity(referralMapper.toDto(expected));
        assertReferralAllPropertiesEquals(expected, actual);
    }
}
