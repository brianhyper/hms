package com.hyperbrains.hms.domain;

import static com.hyperbrains.hms.domain.DepartmentTestSamples.*;
import static com.hyperbrains.hms.domain.ReferralTestSamples.*;
import static com.hyperbrains.hms.domain.VisitTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ReferralTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Referral.class);
        Referral referral1 = getReferralSample1();
        Referral referral2 = new Referral();
        assertThat(referral1).isNotEqualTo(referral2);

        referral2.setId(referral1.getId());
        assertThat(referral1).isEqualTo(referral2);

        referral2 = getReferralSample2();
        assertThat(referral1).isNotEqualTo(referral2);
    }

    @Test
    void visitTest() {
        Referral referral = getReferralRandomSampleGenerator();
        Visit visitBack = getVisitRandomSampleGenerator();

        referral.setVisit(visitBack);
        assertThat(referral.getVisit()).isEqualTo(visitBack);

        referral.visit(null);
        assertThat(referral.getVisit()).isNull();
    }

    @Test
    void departmentTest() {
        Referral referral = getReferralRandomSampleGenerator();
        Department departmentBack = getDepartmentRandomSampleGenerator();

        referral.setDepartment(departmentBack);
        assertThat(referral.getDepartment()).isEqualTo(departmentBack);

        referral.department(null);
        assertThat(referral.getDepartment()).isNull();
    }
}
