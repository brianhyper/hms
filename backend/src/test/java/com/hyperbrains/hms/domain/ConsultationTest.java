package com.hyperbrains.hms.domain;

import static com.hyperbrains.hms.domain.ConsultationTestSamples.*;
import static com.hyperbrains.hms.domain.DiagnosisTestSamples.*;
import static com.hyperbrains.hms.domain.VisitTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ConsultationTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Consultation.class);
        Consultation consultation1 = getConsultationSample1();
        Consultation consultation2 = new Consultation();
        assertThat(consultation1).isNotEqualTo(consultation2);

        consultation2.setId(consultation1.getId());
        assertThat(consultation1).isEqualTo(consultation2);

        consultation2 = getConsultationSample2();
        assertThat(consultation1).isNotEqualTo(consultation2);
    }

    @Test
    void diagnosesTest() {
        Consultation consultation = getConsultationRandomSampleGenerator();
        Diagnosis diagnosisBack = getDiagnosisRandomSampleGenerator();

        consultation.addDiagnoses(diagnosisBack);
        assertThat(consultation.getDiagnoseses()).containsOnly(diagnosisBack);

        consultation.removeDiagnoses(diagnosisBack);
        assertThat(consultation.getDiagnoseses()).doesNotContain(diagnosisBack);

        consultation.diagnoseses(new HashSet<>(Set.of(diagnosisBack)));
        assertThat(consultation.getDiagnoseses()).containsOnly(diagnosisBack);

        consultation.setDiagnoseses(new HashSet<>());
        assertThat(consultation.getDiagnoseses()).doesNotContain(diagnosisBack);
    }

    @Test
    void visitTest() {
        Consultation consultation = getConsultationRandomSampleGenerator();
        Visit visitBack = getVisitRandomSampleGenerator();

        consultation.setVisit(visitBack);
        assertThat(consultation.getVisit()).isEqualTo(visitBack);
        assertThat(visitBack.getConsultation()).isEqualTo(consultation);

        consultation.visit(null);
        assertThat(consultation.getVisit()).isNull();
        assertThat(visitBack.getConsultation()).isNull();
    }
}
