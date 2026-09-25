package com.hyperbrains.hms.service.mapper;

import static com.hyperbrains.hms.domain.BillLineItemAsserts.*;
import static com.hyperbrains.hms.domain.BillLineItemTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BillLineItemMapperTest {

    private BillLineItemMapper billLineItemMapper;

    @BeforeEach
    void setUp() {
        billLineItemMapper = new BillLineItemMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getBillLineItemSample1();
        var actual = billLineItemMapper.toEntity(billLineItemMapper.toDto(expected));
        assertBillLineItemAllPropertiesEquals(expected, actual);
    }
}
