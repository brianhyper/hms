package com.hyperbrains.hms.domain;

import static com.hyperbrains.hms.domain.HospitalServiceTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class HospitalServiceTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(HospitalService.class);
        HospitalService hospitalService1 = getHospitalServiceSample1();
        HospitalService hospitalService2 = new HospitalService();
        assertThat(hospitalService1).isNotEqualTo(hospitalService2);

        hospitalService2.setId(hospitalService1.getId());
        assertThat(hospitalService1).isEqualTo(hospitalService2);

        hospitalService2 = getHospitalServiceSample2();
        assertThat(hospitalService1).isNotEqualTo(hospitalService2);
    }
}
