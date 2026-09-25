package com.hyperbrains.hms.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ResultDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(ResultDTO.class);
        ResultDTO resultDTO1 = new ResultDTO();
        resultDTO1.setId(1L);
        ResultDTO resultDTO2 = new ResultDTO();
        assertThat(resultDTO1).isNotEqualTo(resultDTO2);
        resultDTO2.setId(resultDTO1.getId());
        assertThat(resultDTO1).isEqualTo(resultDTO2);
        resultDTO2.setId(2L);
        assertThat(resultDTO1).isNotEqualTo(resultDTO2);
        resultDTO1.setId(null);
        assertThat(resultDTO1).isNotEqualTo(resultDTO2);
    }
}
