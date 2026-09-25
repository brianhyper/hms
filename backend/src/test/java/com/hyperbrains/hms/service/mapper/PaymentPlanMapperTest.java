package com.hyperbrains.hms.service.mapper;

import static com.hyperbrains.hms.domain.PaymentPlanAsserts.*;
import static com.hyperbrains.hms.domain.PaymentPlanTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PaymentPlanMapperTest {

    private PaymentPlanMapper paymentPlanMapper;

    @BeforeEach
    void setUp() {
        paymentPlanMapper = new PaymentPlanMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getPaymentPlanSample1();
        var actual = paymentPlanMapper.toEntity(paymentPlanMapper.toDto(expected));
        assertPaymentPlanAllPropertiesEquals(expected, actual);
    }
}
