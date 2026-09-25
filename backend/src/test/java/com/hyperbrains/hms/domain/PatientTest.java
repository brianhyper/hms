package com.hyperbrains.hms.domain;

import static com.hyperbrains.hms.domain.PatientTestSamples.*;
import static com.hyperbrains.hms.domain.PatientTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class PatientTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Patient.class);
        Patient patient1 = getPatientSample1();
        Patient patient2 = new Patient();
        assertThat(patient1).isNotEqualTo(patient2);

        patient2.setId(patient1.getId());
        assertThat(patient1).isEqualTo(patient2);

        patient2 = getPatientSample2();
        assertThat(patient1).isNotEqualTo(patient2);
    }

    @Test
    void mergedIntoPatientTest() {
        Patient patient = getPatientRandomSampleGenerator();
        Patient patientBack = getPatientRandomSampleGenerator();

        patient.setMergedIntoPatient(patientBack);
        assertThat(patient.getMergedIntoPatient()).isEqualTo(patientBack);

        patient.mergedIntoPatient(null);
        assertThat(patient.getMergedIntoPatient()).isNull();
    }
}
