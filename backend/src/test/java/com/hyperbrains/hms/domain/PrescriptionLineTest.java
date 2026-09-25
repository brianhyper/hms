package com.hyperbrains.hms.domain;

import static com.hyperbrains.hms.domain.DrugTestSamples.*;
import static com.hyperbrains.hms.domain.PrescriptionLineTestSamples.*;
import static com.hyperbrains.hms.domain.PrescriptionTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class PrescriptionLineTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(PrescriptionLine.class);
        PrescriptionLine prescriptionLine1 = getPrescriptionLineSample1();
        PrescriptionLine prescriptionLine2 = new PrescriptionLine();
        assertThat(prescriptionLine1).isNotEqualTo(prescriptionLine2);

        prescriptionLine2.setId(prescriptionLine1.getId());
        assertThat(prescriptionLine1).isEqualTo(prescriptionLine2);

        prescriptionLine2 = getPrescriptionLineSample2();
        assertThat(prescriptionLine1).isNotEqualTo(prescriptionLine2);
    }

    @Test
    void prescriptionTest() {
        PrescriptionLine prescriptionLine = getPrescriptionLineRandomSampleGenerator();
        Prescription prescriptionBack = getPrescriptionRandomSampleGenerator();

        prescriptionLine.setPrescription(prescriptionBack);
        assertThat(prescriptionLine.getPrescription()).isEqualTo(prescriptionBack);

        prescriptionLine.prescription(null);
        assertThat(prescriptionLine.getPrescription()).isNull();
    }

    @Test
    void drugTest() {
        PrescriptionLine prescriptionLine = getPrescriptionLineRandomSampleGenerator();
        Drug drugBack = getDrugRandomSampleGenerator();

        prescriptionLine.setDrug(drugBack);
        assertThat(prescriptionLine.getDrug()).isEqualTo(drugBack);

        prescriptionLine.drug(null);
        assertThat(prescriptionLine.getDrug()).isNull();
    }
}
