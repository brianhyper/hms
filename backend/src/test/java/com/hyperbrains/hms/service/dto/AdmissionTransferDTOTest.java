package com.hyperbrains.hms.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class AdmissionTransferDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(AdmissionTransferDTO.class);
        AdmissionTransferDTO admissionTransferDTO1 = new AdmissionTransferDTO();
        admissionTransferDTO1.setId(1L);
        AdmissionTransferDTO admissionTransferDTO2 = new AdmissionTransferDTO();
        assertThat(admissionTransferDTO1).isNotEqualTo(admissionTransferDTO2);
        admissionTransferDTO2.setId(admissionTransferDTO1.getId());
        assertThat(admissionTransferDTO1).isEqualTo(admissionTransferDTO2);
        admissionTransferDTO2.setId(2L);
        assertThat(admissionTransferDTO1).isNotEqualTo(admissionTransferDTO2);
        admissionTransferDTO1.setId(null);
        assertThat(admissionTransferDTO1).isNotEqualTo(admissionTransferDTO2);
    }
}
