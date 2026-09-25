package com.hyperbrains.hms.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class LabTestDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(LabTestDTO.class);
        LabTestDTO labTestDTO1 = new LabTestDTO();
        labTestDTO1.setId(1L);
        LabTestDTO labTestDTO2 = new LabTestDTO();
        assertThat(labTestDTO1).isNotEqualTo(labTestDTO2);
        labTestDTO2.setId(labTestDTO1.getId());
        assertThat(labTestDTO1).isEqualTo(labTestDTO2);
        labTestDTO2.setId(2L);
        assertThat(labTestDTO1).isNotEqualTo(labTestDTO2);
        labTestDTO1.setId(null);
        assertThat(labTestDTO1).isNotEqualTo(labTestDTO2);
    }
}
