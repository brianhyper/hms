package com.hyperbrains.hms.domain;

import static com.hyperbrains.hms.domain.AdHocChargeTestSamples.*;
import static com.hyperbrains.hms.domain.AdmissionTestSamples.*;
import static com.hyperbrains.hms.domain.HospitalServiceTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class AdHocChargeTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(AdHocCharge.class);
        AdHocCharge adHocCharge1 = getAdHocChargeSample1();
        AdHocCharge adHocCharge2 = new AdHocCharge();
        assertThat(adHocCharge1).isNotEqualTo(adHocCharge2);

        adHocCharge2.setId(adHocCharge1.getId());
        assertThat(adHocCharge1).isEqualTo(adHocCharge2);

        adHocCharge2 = getAdHocChargeSample2();
        assertThat(adHocCharge1).isNotEqualTo(adHocCharge2);
    }

    @Test
    void admissionTest() {
        AdHocCharge adHocCharge = getAdHocChargeRandomSampleGenerator();
        Admission admissionBack = getAdmissionRandomSampleGenerator();

        adHocCharge.setAdmission(admissionBack);
        assertThat(adHocCharge.getAdmission()).isEqualTo(admissionBack);

        adHocCharge.admission(null);
        assertThat(adHocCharge.getAdmission()).isNull();
    }

    @Test
    void serviceCatalogueTest() {
        AdHocCharge adHocCharge = getAdHocChargeRandomSampleGenerator();
        HospitalService hospitalServiceBack = getHospitalServiceRandomSampleGenerator();

        adHocCharge.setServiceCatalogue(hospitalServiceBack);
        assertThat(adHocCharge.getServiceCatalogue()).isEqualTo(hospitalServiceBack);

        adHocCharge.serviceCatalogue(null);
        assertThat(adHocCharge.getServiceCatalogue()).isNull();
    }
}
