package com.hyperbrains.hms.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class PaymentPlanDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(PaymentPlanDTO.class);
        PaymentPlanDTO paymentPlanDTO1 = new PaymentPlanDTO();
        paymentPlanDTO1.setId(1L);
        PaymentPlanDTO paymentPlanDTO2 = new PaymentPlanDTO();
        assertThat(paymentPlanDTO1).isNotEqualTo(paymentPlanDTO2);
        paymentPlanDTO2.setId(paymentPlanDTO1.getId());
        assertThat(paymentPlanDTO1).isEqualTo(paymentPlanDTO2);
        paymentPlanDTO2.setId(2L);
        assertThat(paymentPlanDTO1).isNotEqualTo(paymentPlanDTO2);
        paymentPlanDTO1.setId(null);
        assertThat(paymentPlanDTO1).isNotEqualTo(paymentPlanDTO2);
    }
}
