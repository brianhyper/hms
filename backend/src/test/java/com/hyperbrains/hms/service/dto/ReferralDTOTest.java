package com.hyperbrains.hms.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ReferralDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(ReferralDTO.class);
        ReferralDTO referralDTO1 = new ReferralDTO();
        referralDTO1.setId(1L);
        ReferralDTO referralDTO2 = new ReferralDTO();
        assertThat(referralDTO1).isNotEqualTo(referralDTO2);
        referralDTO2.setId(referralDTO1.getId());
        assertThat(referralDTO1).isEqualTo(referralDTO2);
        referralDTO2.setId(2L);
        assertThat(referralDTO1).isNotEqualTo(referralDTO2);
        referralDTO1.setId(null);
        assertThat(referralDTO1).isNotEqualTo(referralDTO2);
    }
}
