package com.hyperbrains.hms.service.mapper;

import static com.hyperbrains.hms.domain.OrderExecutionAsserts.*;
import static com.hyperbrains.hms.domain.OrderExecutionTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OrderExecutionMapperTest {

    private OrderExecutionMapper orderExecutionMapper;

    @BeforeEach
    void setUp() {
        orderExecutionMapper = new OrderExecutionMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getOrderExecutionSample1();
        var actual = orderExecutionMapper.toEntity(orderExecutionMapper.toDto(expected));
        assertOrderExecutionAllPropertiesEquals(expected, actual);
    }
}
