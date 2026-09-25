package com.hyperbrains.hms.domain;

import static com.hyperbrains.hms.domain.DepartmentTestSamples.*;
import static com.hyperbrains.hms.domain.WardTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class WardTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Ward.class);
        Ward ward1 = getWardSample1();
        Ward ward2 = new Ward();
        assertThat(ward1).isNotEqualTo(ward2);

        ward2.setId(ward1.getId());
        assertThat(ward1).isEqualTo(ward2);

        ward2 = getWardSample2();
        assertThat(ward1).isNotEqualTo(ward2);
    }

    @Test
    void departmentTest() {
        Ward ward = getWardRandomSampleGenerator();
        Department departmentBack = getDepartmentRandomSampleGenerator();

        ward.setDepartment(departmentBack);
        assertThat(ward.getDepartment()).isEqualTo(departmentBack);

        ward.department(null);
        assertThat(ward.getDepartment()).isNull();
    }
}
