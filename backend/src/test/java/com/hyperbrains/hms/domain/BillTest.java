package com.hyperbrains.hms.domain;

import static com.hyperbrains.hms.domain.BillTestSamples.*;
import static com.hyperbrains.hms.domain.PaymentTestSamples.*;
import static com.hyperbrains.hms.domain.VisitTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class BillTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Bill.class);
        Bill bill1 = getBillSample1();
        Bill bill2 = new Bill();
        assertThat(bill1).isNotEqualTo(bill2);

        bill2.setId(bill1.getId());
        assertThat(bill1).isEqualTo(bill2);

        bill2 = getBillSample2();
        assertThat(bill1).isNotEqualTo(bill2);
    }

    @Test
    void paymentTest() {
        Bill bill = getBillRandomSampleGenerator();
        Payment paymentBack = getPaymentRandomSampleGenerator();

        bill.setPayment(paymentBack);
        assertThat(bill.getPayment()).isEqualTo(paymentBack);

        bill.payment(null);
        assertThat(bill.getPayment()).isNull();
    }

    @Test
    void visitTest() {
        Bill bill = getBillRandomSampleGenerator();
        Visit visitBack = getVisitRandomSampleGenerator();

        bill.setVisit(visitBack);
        assertThat(bill.getVisit()).isEqualTo(visitBack);
        assertThat(visitBack.getBill()).isEqualTo(bill);

        bill.visit(null);
        assertThat(bill.getVisit()).isNull();
        assertThat(visitBack.getBill()).isNull();
    }
}
