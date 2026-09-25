package com.hyperbrains.hms.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class DiagnosisDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(DiagnosisDTO.class);
        DiagnosisDTO diagnosisDTO1 = new DiagnosisDTO();
        diagnosisDTO1.setId(1L);
        DiagnosisDTO diagnosisDTO2 = new DiagnosisDTO();
        assertThat(diagnosisDTO1).isNotEqualTo(diagnosisDTO2);
        diagnosisDTO2.setId(diagnosisDTO1.getId());
        assertThat(diagnosisDTO1).isEqualTo(diagnosisDTO2);
        diagnosisDTO2.setId(2L);
        assertThat(diagnosisDTO1).isNotEqualTo(diagnosisDTO2);
        diagnosisDTO1.setId(null);
        assertThat(diagnosisDTO1).isNotEqualTo(diagnosisDTO2);
    }
}
