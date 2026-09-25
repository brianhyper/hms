package com.hyperbrains.hms.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class WardCoverDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(WardCoverDTO.class);
        WardCoverDTO wardCoverDTO1 = new WardCoverDTO();
        wardCoverDTO1.setId(1L);
        WardCoverDTO wardCoverDTO2 = new WardCoverDTO();
        assertThat(wardCoverDTO1).isNotEqualTo(wardCoverDTO2);
        wardCoverDTO2.setId(wardCoverDTO1.getId());
        assertThat(wardCoverDTO1).isEqualTo(wardCoverDTO2);
        wardCoverDTO2.setId(2L);
        assertThat(wardCoverDTO1).isNotEqualTo(wardCoverDTO2);
        wardCoverDTO1.setId(null);
        assertThat(wardCoverDTO1).isNotEqualTo(wardCoverDTO2);
    }
}
