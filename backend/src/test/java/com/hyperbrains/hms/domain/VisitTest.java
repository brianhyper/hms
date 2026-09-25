package com.hyperbrains.hms.domain;

import static com.hyperbrains.hms.domain.AppointmentTestSamples.*;
import static com.hyperbrains.hms.domain.BillTestSamples.*;
import static com.hyperbrains.hms.domain.ConsultationTestSamples.*;
import static com.hyperbrains.hms.domain.PatientTestSamples.*;
import static com.hyperbrains.hms.domain.VisitTestSamples.*;
import static com.hyperbrains.hms.domain.VitalSignsTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class VisitTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Visit.class);
        Visit visit1 = getVisitSample1();
        Visit visit2 = new Visit();
        assertThat(visit1).isNotEqualTo(visit2);

        visit2.setId(visit1.getId());
        assertThat(visit1).isEqualTo(visit2);

        visit2 = getVisitSample2();
        assertThat(visit1).isNotEqualTo(visit2);
    }

    @Test
    void vitalsTest() {
        Visit visit = getVisitRandomSampleGenerator();
        VitalSigns vitalSignsBack = getVitalSignsRandomSampleGenerator();

        visit.setVitals(vitalSignsBack);
        assertThat(visit.getVitals()).isEqualTo(vitalSignsBack);

        visit.vitals(null);
        assertThat(visit.getVitals()).isNull();
    }

    @Test
    void consultationTest() {
        Visit visit = getVisitRandomSampleGenerator();
        Consultation consultationBack = getConsultationRandomSampleGenerator();

        visit.setConsultation(consultationBack);
        assertThat(visit.getConsultation()).isEqualTo(consultationBack);

        visit.consultation(null);
        assertThat(visit.getConsultation()).isNull();
    }

    @Test
    void billTest() {
        Visit visit = getVisitRandomSampleGenerator();
        Bill billBack = getBillRandomSampleGenerator();

        visit.setBill(billBack);
        assertThat(visit.getBill()).isEqualTo(billBack);

        visit.bill(null);
        assertThat(visit.getBill()).isNull();
    }

    @Test
    void patientTest() {
        Visit visit = getVisitRandomSampleGenerator();
        Patient patientBack = getPatientRandomSampleGenerator();

        visit.setPatient(patientBack);
        assertThat(visit.getPatient()).isEqualTo(patientBack);

        visit.patient(null);
        assertThat(visit.getPatient()).isNull();
    }

    @Test
    void appointmentTest() {
        Visit visit = getVisitRandomSampleGenerator();
        Appointment appointmentBack = getAppointmentRandomSampleGenerator();

        visit.setAppointment(appointmentBack);
        assertThat(visit.getAppointment()).isEqualTo(appointmentBack);
        assertThat(appointmentBack.getVisit()).isEqualTo(visit);

        visit.appointment(null);
        assertThat(visit.getAppointment()).isNull();
        assertThat(appointmentBack.getVisit()).isNull();
    }
}
