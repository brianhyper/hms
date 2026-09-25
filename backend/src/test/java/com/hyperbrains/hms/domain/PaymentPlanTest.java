package com.hyperbrains.hms.domain;

import static com.hyperbrains.hms.domain.BillTestSamples.*;
import static com.hyperbrains.hms.domain.PaymentPlanTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class PaymentPlanTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(PaymentPlan.class);
        PaymentPlan paymentPlan1 = getPaymentPlanSample1();
        PaymentPlan paymentPlan2 = new PaymentPlan();
        assertThat(paymentPlan1).isNotEqualTo(paymentPlan2);

        paymentPlan2.setId(paymentPlan1.getId());
        assertThat(paymentPlan1).isEqualTo(paymentPlan2);

        paymentPlan2 = getPaymentPlanSample2();
        assertThat(paymentPlan1).isNotEqualTo(paymentPlan2);
    }

    @Test
    void billTest() {
        PaymentPlan paymentPlan = getPaymentPlanRandomSampleGenerator();
        Bill billBack = getBillRandomSampleGenerator();

        paymentPlan.setBill(billBack);
        assertThat(paymentPlan.getBill()).isEqualTo(billBack);

        paymentPlan.bill(null);
        assertThat(paymentPlan.getBill()).isNull();
    }
}
