package com.hyperbrains.hms.domain;

import static com.hyperbrains.hms.domain.DoctorOrderTestSamples.*;
import static com.hyperbrains.hms.domain.OrderExecutionTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class OrderExecutionTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(OrderExecution.class);
        OrderExecution orderExecution1 = getOrderExecutionSample1();
        OrderExecution orderExecution2 = new OrderExecution();
        assertThat(orderExecution1).isNotEqualTo(orderExecution2);

        orderExecution2.setId(orderExecution1.getId());
        assertThat(orderExecution1).isEqualTo(orderExecution2);

        orderExecution2 = getOrderExecutionSample2();
        assertThat(orderExecution1).isNotEqualTo(orderExecution2);
    }

    @Test
    void doctorOrderTest() {
        OrderExecution orderExecution = getOrderExecutionRandomSampleGenerator();
        DoctorOrder doctorOrderBack = getDoctorOrderRandomSampleGenerator();

        orderExecution.setDoctorOrder(doctorOrderBack);
        assertThat(orderExecution.getDoctorOrder()).isEqualTo(doctorOrderBack);

        orderExecution.doctorOrder(null);
        assertThat(orderExecution.getDoctorOrder()).isNull();
    }
}
