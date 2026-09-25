package com.hyperbrains.hms.domain;

import static com.hyperbrains.hms.domain.AdmissionTestSamples.*;
import static com.hyperbrains.hms.domain.BedTestSamples.*;
import static com.hyperbrains.hms.domain.VisitTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class AdmissionTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Admission.class);
        Admission admission1 = getAdmissionSample1();
        Admission admission2 = new Admission();
        assertThat(admission1).isNotEqualTo(admission2);

        admission2.setId(admission1.getId());
        assertThat(admission1).isEqualTo(admission2);

        admission2 = getAdmissionSample2();
        assertThat(admission1).isNotEqualTo(admission2);
    }

    @Test
    void visitTest() {
        Admission admission = getAdmissionRandomSampleGenerator();
        Visit visitBack = getVisitRandomSampleGenerator();

        admission.setVisit(visitBack);
        assertThat(admission.getVisit()).isEqualTo(visitBack);

        admission.visit(null);
        assertThat(admission.getVisit()).isNull();
    }

    @Test
    void bedTest() {
        Admission admission = getAdmissionRandomSampleGenerator();
        Bed bedBack = getBedRandomSampleGenerator();

        admission.setBed(bedBack);
        assertThat(admission.getBed()).isEqualTo(bedBack);

        admission.bed(null);
        assertThat(admission.getBed()).isNull();
    }
}
