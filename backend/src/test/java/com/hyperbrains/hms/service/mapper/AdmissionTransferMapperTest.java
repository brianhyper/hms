package com.hyperbrains.hms.service.mapper;

import static com.hyperbrains.hms.domain.AdmissionTransferAsserts.*;
import static com.hyperbrains.hms.domain.AdmissionTransferTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AdmissionTransferMapperTest {

    private AdmissionTransferMapper admissionTransferMapper;

    @BeforeEach
    void setUp() {
        admissionTransferMapper = new AdmissionTransferMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getAdmissionTransferSample1();
        var actual = admissionTransferMapper.toEntity(admissionTransferMapper.toDto(expected));
        assertAdmissionTransferAllPropertiesEquals(expected, actual);
    }
}
