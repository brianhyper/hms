package com.hyperbrains.hms.domain;

import static com.hyperbrains.hms.domain.AdmissionTestSamples.*;
import static com.hyperbrains.hms.domain.DoctorOrderTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class DoctorOrderTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(DoctorOrder.class);
        DoctorOrder doctorOrder1 = getDoctorOrderSample1();
        DoctorOrder doctorOrder2 = new DoctorOrder();
        assertThat(doctorOrder1).isNotEqualTo(doctorOrder2);

        doctorOrder2.setId(doctorOrder1.getId());
        assertThat(doctorOrder1).isEqualTo(doctorOrder2);

        doctorOrder2 = getDoctorOrderSample2();
        assertThat(doctorOrder1).isNotEqualTo(doctorOrder2);
    }

    @Test
    void admissionTest() {
        DoctorOrder doctorOrder = getDoctorOrderRandomSampleGenerator();
        Admission admissionBack = getAdmissionRandomSampleGenerator();

        doctorOrder.setAdmission(admissionBack);
        assertThat(doctorOrder.getAdmission()).isEqualTo(admissionBack);

        doctorOrder.admission(null);
        assertThat(doctorOrder.getAdmission()).isNull();
    }
}
