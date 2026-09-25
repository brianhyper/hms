package com.hyperbrains.hms.domain;

import static com.hyperbrains.hms.domain.DispenseTestSamples.*;
import static com.hyperbrains.hms.domain.PrescriptionTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class DispenseTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Dispense.class);
        Dispense dispense1 = getDispenseSample1();
        Dispense dispense2 = new Dispense();
        assertThat(dispense1).isNotEqualTo(dispense2);

        dispense2.setId(dispense1.getId());
        assertThat(dispense1).isEqualTo(dispense2);

        dispense2 = getDispenseSample2();
        assertThat(dispense1).isNotEqualTo(dispense2);
    }

    @Test
    void prescriptionTest() {
        Dispense dispense = getDispenseRandomSampleGenerator();
        Prescription prescriptionBack = getPrescriptionRandomSampleGenerator();

        dispense.setPrescription(prescriptionBack);
        assertThat(dispense.getPrescription()).isEqualTo(prescriptionBack);

        dispense.prescription(null);
        assertThat(dispense.getPrescription()).isNull();
    }
}
