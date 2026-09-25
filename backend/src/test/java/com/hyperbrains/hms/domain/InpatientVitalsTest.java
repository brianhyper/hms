package com.hyperbrains.hms.domain;

import static com.hyperbrains.hms.domain.AdmissionTestSamples.*;
import static com.hyperbrains.hms.domain.InpatientVitalsTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class InpatientVitalsTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(InpatientVitals.class);
        InpatientVitals inpatientVitals1 = getInpatientVitalsSample1();
        InpatientVitals inpatientVitals2 = new InpatientVitals();
        assertThat(inpatientVitals1).isNotEqualTo(inpatientVitals2);

        inpatientVitals2.setId(inpatientVitals1.getId());
        assertThat(inpatientVitals1).isEqualTo(inpatientVitals2);

        inpatientVitals2 = getInpatientVitalsSample2();
        assertThat(inpatientVitals1).isNotEqualTo(inpatientVitals2);
    }

    @Test
    void admissionTest() {
        InpatientVitals inpatientVitals = getInpatientVitalsRandomSampleGenerator();
        Admission admissionBack = getAdmissionRandomSampleGenerator();

        inpatientVitals.setAdmission(admissionBack);
        assertThat(inpatientVitals.getAdmission()).isEqualTo(admissionBack);

        inpatientVitals.admission(null);
        assertThat(inpatientVitals.getAdmission()).isNull();
    }
}
