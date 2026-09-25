package com.hyperbrains.hms.domain;

import static com.hyperbrains.hms.domain.BedTestSamples.*;
import static com.hyperbrains.hms.domain.BedTypeTestSamples.*;
import static com.hyperbrains.hms.domain.WardTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class BedTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Bed.class);
        Bed bed1 = getBedSample1();
        Bed bed2 = new Bed();
        assertThat(bed1).isNotEqualTo(bed2);

        bed2.setId(bed1.getId());
        assertThat(bed1).isEqualTo(bed2);

        bed2 = getBedSample2();
        assertThat(bed1).isNotEqualTo(bed2);
    }

    @Test
    void wardTest() {
        Bed bed = getBedRandomSampleGenerator();
        Ward wardBack = getWardRandomSampleGenerator();

        bed.setWard(wardBack);
        assertThat(bed.getWard()).isEqualTo(wardBack);

        bed.ward(null);
        assertThat(bed.getWard()).isNull();
    }

    @Test
    void bedTypeTest() {
        Bed bed = getBedRandomSampleGenerator();
        BedType bedTypeBack = getBedTypeRandomSampleGenerator();

        bed.setBedType(bedTypeBack);
        assertThat(bed.getBedType()).isEqualTo(bedTypeBack);

        bed.bedType(null);
        assertThat(bed.getBedType()).isNull();
    }
}
