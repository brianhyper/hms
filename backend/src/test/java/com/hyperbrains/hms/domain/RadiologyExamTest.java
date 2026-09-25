package com.hyperbrains.hms.domain;

import static com.hyperbrains.hms.domain.RadiologyExamTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class RadiologyExamTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(RadiologyExam.class);
        RadiologyExam radiologyExam1 = getRadiologyExamSample1();
        RadiologyExam radiologyExam2 = new RadiologyExam();
        assertThat(radiologyExam1).isNotEqualTo(radiologyExam2);

        radiologyExam2.setId(radiologyExam1.getId());
        assertThat(radiologyExam1).isEqualTo(radiologyExam2);

        radiologyExam2 = getRadiologyExamSample2();
        assertThat(radiologyExam1).isNotEqualTo(radiologyExam2);
    }
}
