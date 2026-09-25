package com.hyperbrains.hms.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class AdHocChargeDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(AdHocChargeDTO.class);
        AdHocChargeDTO adHocChargeDTO1 = new AdHocChargeDTO();
        adHocChargeDTO1.setId(1L);
        AdHocChargeDTO adHocChargeDTO2 = new AdHocChargeDTO();
        assertThat(adHocChargeDTO1).isNotEqualTo(adHocChargeDTO2);
        adHocChargeDTO2.setId(adHocChargeDTO1.getId());
        assertThat(adHocChargeDTO1).isEqualTo(adHocChargeDTO2);
        adHocChargeDTO2.setId(2L);
        assertThat(adHocChargeDTO1).isNotEqualTo(adHocChargeDTO2);
        adHocChargeDTO1.setId(null);
        assertThat(adHocChargeDTO1).isNotEqualTo(adHocChargeDTO2);
    }
}
