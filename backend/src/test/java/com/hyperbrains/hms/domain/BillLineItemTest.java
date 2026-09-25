package com.hyperbrains.hms.domain;

import static com.hyperbrains.hms.domain.BillLineItemTestSamples.*;
import static com.hyperbrains.hms.domain.BillTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class BillLineItemTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(BillLineItem.class);
        BillLineItem billLineItem1 = getBillLineItemSample1();
        BillLineItem billLineItem2 = new BillLineItem();
        assertThat(billLineItem1).isNotEqualTo(billLineItem2);

        billLineItem2.setId(billLineItem1.getId());
        assertThat(billLineItem1).isEqualTo(billLineItem2);

        billLineItem2 = getBillLineItemSample2();
        assertThat(billLineItem1).isNotEqualTo(billLineItem2);
    }

    @Test
    void billTest() {
        BillLineItem billLineItem = getBillLineItemRandomSampleGenerator();
        Bill billBack = getBillRandomSampleGenerator();

        billLineItem.setBill(billBack);
        assertThat(billLineItem.getBill()).isEqualTo(billBack);

        billLineItem.bill(null);
        assertThat(billLineItem.getBill()).isNull();
    }
}
