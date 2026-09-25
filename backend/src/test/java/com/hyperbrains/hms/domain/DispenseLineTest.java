package com.hyperbrains.hms.domain;

import static com.hyperbrains.hms.domain.DispenseLineTestSamples.*;
import static com.hyperbrains.hms.domain.DispenseTestSamples.*;
import static com.hyperbrains.hms.domain.DrugTestSamples.*;
import static com.hyperbrains.hms.domain.PrescriptionLineTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class DispenseLineTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(DispenseLine.class);
        DispenseLine dispenseLine1 = getDispenseLineSample1();
        DispenseLine dispenseLine2 = new DispenseLine();
        assertThat(dispenseLine1).isNotEqualTo(dispenseLine2);

        dispenseLine2.setId(dispenseLine1.getId());
        assertThat(dispenseLine1).isEqualTo(dispenseLine2);

        dispenseLine2 = getDispenseLineSample2();
        assertThat(dispenseLine1).isNotEqualTo(dispenseLine2);
    }

    @Test
    void dispenseTest() {
        DispenseLine dispenseLine = getDispenseLineRandomSampleGenerator();
        Dispense dispenseBack = getDispenseRandomSampleGenerator();

        dispenseLine.setDispense(dispenseBack);
        assertThat(dispenseLine.getDispense()).isEqualTo(dispenseBack);

        dispenseLine.dispense(null);
        assertThat(dispenseLine.getDispense()).isNull();
    }

    @Test
    void prescriptionLineTest() {
        DispenseLine dispenseLine = getDispenseLineRandomSampleGenerator();
        PrescriptionLine prescriptionLineBack = getPrescriptionLineRandomSampleGenerator();

        dispenseLine.setPrescriptionLine(prescriptionLineBack);
        assertThat(dispenseLine.getPrescriptionLine()).isEqualTo(prescriptionLineBack);

        dispenseLine.prescriptionLine(null);
        assertThat(dispenseLine.getPrescriptionLine()).isNull();
    }

    @Test
    void drugTest() {
        DispenseLine dispenseLine = getDispenseLineRandomSampleGenerator();
        Drug drugBack = getDrugRandomSampleGenerator();

        dispenseLine.setDrug(drugBack);
        assertThat(dispenseLine.getDrug()).isEqualTo(drugBack);

        dispenseLine.drug(null);
        assertThat(dispenseLine.getDrug()).isNull();
    }
}
