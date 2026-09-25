package com.hyperbrains.hms.domain;

import static com.hyperbrains.hms.domain.DiagnosticOrderTestSamples.*;
import static com.hyperbrains.hms.domain.LabTestTestSamples.*;
import static com.hyperbrains.hms.domain.RadiologyExamTestSamples.*;
import static com.hyperbrains.hms.domain.ResultTestSamples.*;
import static com.hyperbrains.hms.domain.VisitTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class DiagnosticOrderTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(DiagnosticOrder.class);
        DiagnosticOrder diagnosticOrder1 = getDiagnosticOrderSample1();
        DiagnosticOrder diagnosticOrder2 = new DiagnosticOrder();
        assertThat(diagnosticOrder1).isNotEqualTo(diagnosticOrder2);

        diagnosticOrder2.setId(diagnosticOrder1.getId());
        assertThat(diagnosticOrder1).isEqualTo(diagnosticOrder2);

        diagnosticOrder2 = getDiagnosticOrderSample2();
        assertThat(diagnosticOrder1).isNotEqualTo(diagnosticOrder2);
    }

    @Test
    void resultTest() {
        DiagnosticOrder diagnosticOrder = getDiagnosticOrderRandomSampleGenerator();
        Result resultBack = getResultRandomSampleGenerator();

        diagnosticOrder.setResult(resultBack);
        assertThat(diagnosticOrder.getResult()).isEqualTo(resultBack);

        diagnosticOrder.result(null);
        assertThat(diagnosticOrder.getResult()).isNull();
    }

    @Test
    void visitTest() {
        DiagnosticOrder diagnosticOrder = getDiagnosticOrderRandomSampleGenerator();
        Visit visitBack = getVisitRandomSampleGenerator();

        diagnosticOrder.setVisit(visitBack);
        assertThat(diagnosticOrder.getVisit()).isEqualTo(visitBack);

        diagnosticOrder.visit(null);
        assertThat(diagnosticOrder.getVisit()).isNull();
    }

    @Test
    void labTestTest() {
        DiagnosticOrder diagnosticOrder = getDiagnosticOrderRandomSampleGenerator();
        LabTest labTestBack = getLabTestRandomSampleGenerator();

        diagnosticOrder.setLabTest(labTestBack);
        assertThat(diagnosticOrder.getLabTest()).isEqualTo(labTestBack);

        diagnosticOrder.labTest(null);
        assertThat(diagnosticOrder.getLabTest()).isNull();
    }

    @Test
    void radiologyExamTest() {
        DiagnosticOrder diagnosticOrder = getDiagnosticOrderRandomSampleGenerator();
        RadiologyExam radiologyExamBack = getRadiologyExamRandomSampleGenerator();

        diagnosticOrder.setRadiologyExam(radiologyExamBack);
        assertThat(diagnosticOrder.getRadiologyExam()).isEqualTo(radiologyExamBack);

        diagnosticOrder.radiologyExam(null);
        assertThat(diagnosticOrder.getRadiologyExam()).isNull();
    }
}
