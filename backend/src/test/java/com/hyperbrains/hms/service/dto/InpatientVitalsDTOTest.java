package com.hyperbrains.hms.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class InpatientVitalsDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(InpatientVitalsDTO.class);
        InpatientVitalsDTO inpatientVitalsDTO1 = new InpatientVitalsDTO();
        inpatientVitalsDTO1.setId(1L);
        InpatientVitalsDTO inpatientVitalsDTO2 = new InpatientVitalsDTO();
        assertThat(inpatientVitalsDTO1).isNotEqualTo(inpatientVitalsDTO2);
        inpatientVitalsDTO2.setId(inpatientVitalsDTO1.getId());
        assertThat(inpatientVitalsDTO1).isEqualTo(inpatientVitalsDTO2);
        inpatientVitalsDTO2.setId(2L);
        assertThat(inpatientVitalsDTO1).isNotEqualTo(inpatientVitalsDTO2);
        inpatientVitalsDTO1.setId(null);
        assertThat(inpatientVitalsDTO1).isNotEqualTo(inpatientVitalsDTO2);
    }
}
