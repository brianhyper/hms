package com.hyperbrains.hms.domain;

import static com.hyperbrains.hms.domain.VisitTestSamples.*;
import static com.hyperbrains.hms.domain.VitalSignsTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class VitalSignsTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(VitalSigns.class);
        VitalSigns vitalSigns1 = getVitalSignsSample1();
        VitalSigns vitalSigns2 = new VitalSigns();
        assertThat(vitalSigns1).isNotEqualTo(vitalSigns2);

        vitalSigns2.setId(vitalSigns1.getId());
        assertThat(vitalSigns1).isEqualTo(vitalSigns2);

        vitalSigns2 = getVitalSignsSample2();
        assertThat(vitalSigns1).isNotEqualTo(vitalSigns2);
    }

    @Test
    void visitTest() {
        VitalSigns vitalSigns = getVitalSignsRandomSampleGenerator();
        Visit visitBack = getVisitRandomSampleGenerator();

        vitalSigns.setVisit(visitBack);
        assertThat(vitalSigns.getVisit()).isEqualTo(visitBack);
        assertThat(visitBack.getVitals()).isEqualTo(vitalSigns);

        vitalSigns.visit(null);
        assertThat(vitalSigns.getVisit()).isNull();
        assertThat(visitBack.getVitals()).isNull();
    }
}
