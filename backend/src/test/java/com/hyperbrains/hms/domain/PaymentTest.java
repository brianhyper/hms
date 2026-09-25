package com.hyperbrains.hms.domain;

import static com.hyperbrains.hms.domain.BillTestSamples.*;
import static com.hyperbrains.hms.domain.PaymentTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class PaymentTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Payment.class);
        Payment payment1 = getPaymentSample1();
        Payment payment2 = new Payment();
        assertThat(payment1).isNotEqualTo(payment2);

        payment2.setId(payment1.getId());
        assertThat(payment1).isEqualTo(payment2);

        payment2 = getPaymentSample2();
        assertThat(payment1).isNotEqualTo(payment2);
    }

    @Test
    void billTest() {
        Payment payment = getPaymentRandomSampleGenerator();
        Bill billBack = getBillRandomSampleGenerator();

        payment.setBill(billBack);
        assertThat(payment.getBill()).isEqualTo(billBack);
        assertThat(billBack.getPayment()).isEqualTo(payment);

        payment.bill(null);
        assertThat(payment.getBill()).isNull();
        assertThat(billBack.getPayment()).isNull();
    }
}
