package com.hyperbrains.hms.domain;

import static com.hyperbrains.hms.domain.ConsultationTestSamples.*;
import static com.hyperbrains.hms.domain.DiagnosisTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class DiagnosisTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Diagnosis.class);
        Diagnosis diagnosis1 = getDiagnosisSample1();
        Diagnosis diagnosis2 = new Diagnosis();
        assertThat(diagnosis1).isNotEqualTo(diagnosis2);

        diagnosis2.setId(diagnosis1.getId());
        assertThat(diagnosis1).isEqualTo(diagnosis2);

        diagnosis2 = getDiagnosisSample2();
        assertThat(diagnosis1).isNotEqualTo(diagnosis2);
    }

    @Test
    void consultationsTest() {
        Diagnosis diagnosis = getDiagnosisRandomSampleGenerator();
        Consultation consultationBack = getConsultationRandomSampleGenerator();

        diagnosis.addConsultations(consultationBack);
        assertThat(diagnosis.getConsultationses()).containsOnly(consultationBack);
        assertThat(consultationBack.getDiagnoseses()).containsOnly(diagnosis);

        diagnosis.removeConsultations(consultationBack);
        assertThat(diagnosis.getConsultationses()).doesNotContain(consultationBack);
        assertThat(consultationBack.getDiagnoseses()).doesNotContain(diagnosis);

        diagnosis.consultationses(new HashSet<>(Set.of(consultationBack)));
        assertThat(diagnosis.getConsultationses()).containsOnly(consultationBack);
        assertThat(consultationBack.getDiagnoseses()).containsOnly(diagnosis);

        diagnosis.setConsultationses(new HashSet<>());
        assertThat(diagnosis.getConsultationses()).doesNotContain(consultationBack);
        assertThat(consultationBack.getDiagnoseses()).doesNotContain(diagnosis);
    }
}
