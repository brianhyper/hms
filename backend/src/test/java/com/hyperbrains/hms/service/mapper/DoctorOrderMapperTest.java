package com.hyperbrains.hms.service.mapper;

import static com.hyperbrains.hms.domain.DoctorOrderAsserts.*;
import static com.hyperbrains.hms.domain.DoctorOrderTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DoctorOrderMapperTest {

    private DoctorOrderMapper doctorOrderMapper;

    @BeforeEach
    void setUp() {
        doctorOrderMapper = new DoctorOrderMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getDoctorOrderSample1();
        var actual = doctorOrderMapper.toEntity(doctorOrderMapper.toDto(expected));
        assertDoctorOrderAllPropertiesEquals(expected, actual);
    }
}
