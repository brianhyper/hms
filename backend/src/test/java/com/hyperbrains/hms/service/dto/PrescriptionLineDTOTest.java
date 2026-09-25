package com.hyperbrains.hms.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class PrescriptionLineDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(PrescriptionLineDTO.class);
        PrescriptionLineDTO prescriptionLineDTO1 = new PrescriptionLineDTO();
        prescriptionLineDTO1.setId(1L);
        PrescriptionLineDTO prescriptionLineDTO2 = new PrescriptionLineDTO();
        assertThat(prescriptionLineDTO1).isNotEqualTo(prescriptionLineDTO2);
        prescriptionLineDTO2.setId(prescriptionLineDTO1.getId());
        assertThat(prescriptionLineDTO1).isEqualTo(prescriptionLineDTO2);
        prescriptionLineDTO2.setId(2L);
        assertThat(prescriptionLineDTO1).isNotEqualTo(prescriptionLineDTO2);
        prescriptionLineDTO1.setId(null);
        assertThat(prescriptionLineDTO1).isNotEqualTo(prescriptionLineDTO2);
    }
}
