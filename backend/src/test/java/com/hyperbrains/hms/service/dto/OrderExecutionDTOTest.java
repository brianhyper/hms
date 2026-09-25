package com.hyperbrains.hms.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class OrderExecutionDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(OrderExecutionDTO.class);
        OrderExecutionDTO orderExecutionDTO1 = new OrderExecutionDTO();
        orderExecutionDTO1.setId(1L);
        OrderExecutionDTO orderExecutionDTO2 = new OrderExecutionDTO();
        assertThat(orderExecutionDTO1).isNotEqualTo(orderExecutionDTO2);
        orderExecutionDTO2.setId(orderExecutionDTO1.getId());
        assertThat(orderExecutionDTO1).isEqualTo(orderExecutionDTO2);
        orderExecutionDTO2.setId(2L);
        assertThat(orderExecutionDTO1).isNotEqualTo(orderExecutionDTO2);
        orderExecutionDTO1.setId(null);
        assertThat(orderExecutionDTO1).isNotEqualTo(orderExecutionDTO2);
    }
}
