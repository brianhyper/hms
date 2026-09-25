package com.hyperbrains.hms.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class DoctorOrderDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(DoctorOrderDTO.class);
        DoctorOrderDTO doctorOrderDTO1 = new DoctorOrderDTO();
        doctorOrderDTO1.setId(1L);
        DoctorOrderDTO doctorOrderDTO2 = new DoctorOrderDTO();
        assertThat(doctorOrderDTO1).isNotEqualTo(doctorOrderDTO2);
        doctorOrderDTO2.setId(doctorOrderDTO1.getId());
        assertThat(doctorOrderDTO1).isEqualTo(doctorOrderDTO2);
        doctorOrderDTO2.setId(2L);
        assertThat(doctorOrderDTO1).isNotEqualTo(doctorOrderDTO2);
        doctorOrderDTO1.setId(null);
        assertThat(doctorOrderDTO1).isNotEqualTo(doctorOrderDTO2);
    }
}
