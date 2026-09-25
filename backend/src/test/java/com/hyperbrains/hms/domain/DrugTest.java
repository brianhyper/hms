package com.hyperbrains.hms.domain;

import static com.hyperbrains.hms.domain.DrugTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class DrugTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Drug.class);
        Drug drug1 = getDrugSample1();
        Drug drug2 = new Drug();
        assertThat(drug1).isNotEqualTo(drug2);

        drug2.setId(drug1.getId());
        assertThat(drug1).isEqualTo(drug2);

        drug2 = getDrugSample2();
        assertThat(drug1).isNotEqualTo(drug2);
    }
}
