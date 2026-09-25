package com.hyperbrains.hms.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class DispenseDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(DispenseDTO.class);
        DispenseDTO dispenseDTO1 = new DispenseDTO();
        dispenseDTO1.setId(1L);
        DispenseDTO dispenseDTO2 = new DispenseDTO();
        assertThat(dispenseDTO1).isNotEqualTo(dispenseDTO2);
        dispenseDTO2.setId(dispenseDTO1.getId());
        assertThat(dispenseDTO1).isEqualTo(dispenseDTO2);
        dispenseDTO2.setId(2L);
        assertThat(dispenseDTO1).isNotEqualTo(dispenseDTO2);
        dispenseDTO1.setId(null);
        assertThat(dispenseDTO1).isNotEqualTo(dispenseDTO2);
    }
}
