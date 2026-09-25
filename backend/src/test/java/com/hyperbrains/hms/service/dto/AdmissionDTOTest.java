package com.hyperbrains.hms.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class AdmissionDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(AdmissionDTO.class);
        AdmissionDTO admissionDTO1 = new AdmissionDTO();
        admissionDTO1.setId(1L);
        AdmissionDTO admissionDTO2 = new AdmissionDTO();
        assertThat(admissionDTO1).isNotEqualTo(admissionDTO2);
        admissionDTO2.setId(admissionDTO1.getId());
        assertThat(admissionDTO1).isEqualTo(admissionDTO2);
        admissionDTO2.setId(2L);
        assertThat(admissionDTO1).isNotEqualTo(admissionDTO2);
        admissionDTO1.setId(null);
        assertThat(admissionDTO1).isNotEqualTo(admissionDTO2);
    }
}
