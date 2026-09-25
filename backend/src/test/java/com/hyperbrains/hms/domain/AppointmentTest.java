package com.hyperbrains.hms.domain;

import static com.hyperbrains.hms.domain.AppointmentTestSamples.*;
import static com.hyperbrains.hms.domain.DepartmentTestSamples.*;
import static com.hyperbrains.hms.domain.PatientTestSamples.*;
import static com.hyperbrains.hms.domain.VisitTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class AppointmentTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Appointment.class);
        Appointment appointment1 = getAppointmentSample1();
        Appointment appointment2 = new Appointment();
        assertThat(appointment1).isNotEqualTo(appointment2);

        appointment2.setId(appointment1.getId());
        assertThat(appointment1).isEqualTo(appointment2);

        appointment2 = getAppointmentSample2();
        assertThat(appointment1).isNotEqualTo(appointment2);
    }

    @Test
    void visitTest() {
        Appointment appointment = getAppointmentRandomSampleGenerator();
        Visit visitBack = getVisitRandomSampleGenerator();

        appointment.setVisit(visitBack);
        assertThat(appointment.getVisit()).isEqualTo(visitBack);

        appointment.visit(null);
        assertThat(appointment.getVisit()).isNull();
    }

    @Test
    void patientTest() {
        Appointment appointment = getAppointmentRandomSampleGenerator();
        Patient patientBack = getPatientRandomSampleGenerator();

        appointment.setPatient(patientBack);
        assertThat(appointment.getPatient()).isEqualTo(patientBack);

        appointment.patient(null);
        assertThat(appointment.getPatient()).isNull();
    }

    @Test
    void departmentTest() {
        Appointment appointment = getAppointmentRandomSampleGenerator();
        Department departmentBack = getDepartmentRandomSampleGenerator();

        appointment.setDepartment(departmentBack);
        assertThat(appointment.getDepartment()).isEqualTo(departmentBack);

        appointment.department(null);
        assertThat(appointment.getDepartment()).isNull();
    }
}
