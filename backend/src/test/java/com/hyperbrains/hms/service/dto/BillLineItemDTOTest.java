package com.hyperbrains.hms.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class BillLineItemDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(BillLineItemDTO.class);
        BillLineItemDTO billLineItemDTO1 = new BillLineItemDTO();
        billLineItemDTO1.setId(1L);
        BillLineItemDTO billLineItemDTO2 = new BillLineItemDTO();
        assertThat(billLineItemDTO1).isNotEqualTo(billLineItemDTO2);
        billLineItemDTO2.setId(billLineItemDTO1.getId());
        assertThat(billLineItemDTO1).isEqualTo(billLineItemDTO2);
        billLineItemDTO2.setId(2L);
        assertThat(billLineItemDTO1).isNotEqualTo(billLineItemDTO2);
        billLineItemDTO1.setId(null);
        assertThat(billLineItemDTO1).isNotEqualTo(billLineItemDTO2);
    }
}
