package com.hyperbrains.hms.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class BedDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(BedDTO.class);
        BedDTO bedDTO1 = new BedDTO();
        bedDTO1.setId(1L);
        BedDTO bedDTO2 = new BedDTO();
        assertThat(bedDTO1).isNotEqualTo(bedDTO2);
        bedDTO2.setId(bedDTO1.getId());
        assertThat(bedDTO1).isEqualTo(bedDTO2);
        bedDTO2.setId(2L);
        assertThat(bedDTO1).isNotEqualTo(bedDTO2);
        bedDTO1.setId(null);
        assertThat(bedDTO1).isNotEqualTo(bedDTO2);
    }
}
