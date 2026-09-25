package com.hyperbrains.hms.domain;

import static com.hyperbrains.hms.domain.BedTypeTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class BedTypeTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(BedType.class);
        BedType bedType1 = getBedTypeSample1();
        BedType bedType2 = new BedType();
        assertThat(bedType1).isNotEqualTo(bedType2);

        bedType2.setId(bedType1.getId());
        assertThat(bedType1).isEqualTo(bedType2);

        bedType2 = getBedTypeSample2();
        assertThat(bedType1).isNotEqualTo(bedType2);
    }
}
