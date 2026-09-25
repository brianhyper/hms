package com.hyperbrains.hms.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class VitalSignsDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(VitalSignsDTO.class);
        VitalSignsDTO vitalSignsDTO1 = new VitalSignsDTO();
        vitalSignsDTO1.setId(1L);
        VitalSignsDTO vitalSignsDTO2 = new VitalSignsDTO();
        assertThat(vitalSignsDTO1).isNotEqualTo(vitalSignsDTO2);
        vitalSignsDTO2.setId(vitalSignsDTO1.getId());
        assertThat(vitalSignsDTO1).isEqualTo(vitalSignsDTO2);
        vitalSignsDTO2.setId(2L);
        assertThat(vitalSignsDTO1).isNotEqualTo(vitalSignsDTO2);
        vitalSignsDTO1.setId(null);
        assertThat(vitalSignsDTO1).isNotEqualTo(vitalSignsDTO2);
    }
}
