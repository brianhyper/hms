package com.hyperbrains.hms.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class DiagnosticOrderDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(DiagnosticOrderDTO.class);
        DiagnosticOrderDTO diagnosticOrderDTO1 = new DiagnosticOrderDTO();
        diagnosticOrderDTO1.setId(1L);
        DiagnosticOrderDTO diagnosticOrderDTO2 = new DiagnosticOrderDTO();
        assertThat(diagnosticOrderDTO1).isNotEqualTo(diagnosticOrderDTO2);
        diagnosticOrderDTO2.setId(diagnosticOrderDTO1.getId());
        assertThat(diagnosticOrderDTO1).isEqualTo(diagnosticOrderDTO2);
        diagnosticOrderDTO2.setId(2L);
        assertThat(diagnosticOrderDTO1).isNotEqualTo(diagnosticOrderDTO2);
        diagnosticOrderDTO1.setId(null);
        assertThat(diagnosticOrderDTO1).isNotEqualTo(diagnosticOrderDTO2);
    }
}
