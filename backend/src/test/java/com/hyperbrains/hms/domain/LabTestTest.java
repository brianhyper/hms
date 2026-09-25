package com.hyperbrains.hms.domain;

import static com.hyperbrains.hms.domain.LabTestTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class LabTestTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(LabTest.class);
        LabTest labTest1 = getLabTestSample1();
        LabTest labTest2 = new LabTest();
        assertThat(labTest1).isNotEqualTo(labTest2);

        labTest2.setId(labTest1.getId());
        assertThat(labTest1).isEqualTo(labTest2);

        labTest2 = getLabTestSample2();
        assertThat(labTest1).isNotEqualTo(labTest2);
    }
}
