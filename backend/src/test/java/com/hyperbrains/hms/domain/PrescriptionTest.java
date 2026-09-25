package com.hyperbrains.hms.domain;

import static com.hyperbrains.hms.domain.PrescriptionTestSamples.*;
import static com.hyperbrains.hms.domain.VisitTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class PrescriptionTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Prescription.class);
        Prescription prescription1 = getPrescriptionSample1();
        Prescription prescription2 = new Prescription();
        assertThat(prescription1).isNotEqualTo(prescription2);

        prescription2.setId(prescription1.getId());
        assertThat(prescription1).isEqualTo(prescription2);

        prescription2 = getPrescriptionSample2();
        assertThat(prescription1).isNotEqualTo(prescription2);
    }

    @Test
    void visitTest() {
        Prescription prescription = getPrescriptionRandomSampleGenerator();
        Visit visitBack = getVisitRandomSampleGenerator();

        prescription.setVisit(visitBack);
        assertThat(prescription.getVisit()).isEqualTo(visitBack);

        prescription.visit(null);
        assertThat(prescription.getVisit()).isNull();
    }
}
