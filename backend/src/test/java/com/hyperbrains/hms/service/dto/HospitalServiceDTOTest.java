package com.hyperbrains.hms.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class HospitalServiceDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(HospitalServiceDTO.class);
        HospitalServiceDTO hospitalServiceDTO1 = new HospitalServiceDTO();
        hospitalServiceDTO1.setId(1L);
        HospitalServiceDTO hospitalServiceDTO2 = new HospitalServiceDTO();
        assertThat(hospitalServiceDTO1).isNotEqualTo(hospitalServiceDTO2);
        hospitalServiceDTO2.setId(hospitalServiceDTO1.getId());
        assertThat(hospitalServiceDTO1).isEqualTo(hospitalServiceDTO2);
        hospitalServiceDTO2.setId(2L);
        assertThat(hospitalServiceDTO1).isNotEqualTo(hospitalServiceDTO2);
        hospitalServiceDTO1.setId(null);
        assertThat(hospitalServiceDTO1).isNotEqualTo(hospitalServiceDTO2);
    }
}
