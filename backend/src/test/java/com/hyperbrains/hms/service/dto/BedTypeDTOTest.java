package com.hyperbrains.hms.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class BedTypeDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(BedTypeDTO.class);
        BedTypeDTO bedTypeDTO1 = new BedTypeDTO();
        bedTypeDTO1.setId(1L);
        BedTypeDTO bedTypeDTO2 = new BedTypeDTO();
        assertThat(bedTypeDTO1).isNotEqualTo(bedTypeDTO2);
        bedTypeDTO2.setId(bedTypeDTO1.getId());
        assertThat(bedTypeDTO1).isEqualTo(bedTypeDTO2);
        bedTypeDTO2.setId(2L);
        assertThat(bedTypeDTO1).isNotEqualTo(bedTypeDTO2);
        bedTypeDTO1.setId(null);
        assertThat(bedTypeDTO1).isNotEqualTo(bedTypeDTO2);
    }
}
