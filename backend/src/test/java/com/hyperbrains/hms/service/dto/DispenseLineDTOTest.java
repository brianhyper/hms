package com.hyperbrains.hms.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class DispenseLineDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(DispenseLineDTO.class);
        DispenseLineDTO dispenseLineDTO1 = new DispenseLineDTO();
        dispenseLineDTO1.setId(1L);
        DispenseLineDTO dispenseLineDTO2 = new DispenseLineDTO();
        assertThat(dispenseLineDTO1).isNotEqualTo(dispenseLineDTO2);
        dispenseLineDTO2.setId(dispenseLineDTO1.getId());
        assertThat(dispenseLineDTO1).isEqualTo(dispenseLineDTO2);
        dispenseLineDTO2.setId(2L);
        assertThat(dispenseLineDTO1).isNotEqualTo(dispenseLineDTO2);
        dispenseLineDTO1.setId(null);
        assertThat(dispenseLineDTO1).isNotEqualTo(dispenseLineDTO2);
    }
}
