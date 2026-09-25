package com.hyperbrains.hms.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class RadiologyExamDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(RadiologyExamDTO.class);
        RadiologyExamDTO radiologyExamDTO1 = new RadiologyExamDTO();
        radiologyExamDTO1.setId(1L);
        RadiologyExamDTO radiologyExamDTO2 = new RadiologyExamDTO();
        assertThat(radiologyExamDTO1).isNotEqualTo(radiologyExamDTO2);
        radiologyExamDTO2.setId(radiologyExamDTO1.getId());
        assertThat(radiologyExamDTO1).isEqualTo(radiologyExamDTO2);
        radiologyExamDTO2.setId(2L);
        assertThat(radiologyExamDTO1).isNotEqualTo(radiologyExamDTO2);
        radiologyExamDTO1.setId(null);
        assertThat(radiologyExamDTO1).isNotEqualTo(radiologyExamDTO2);
    }
}
