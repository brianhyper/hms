package com.hyperbrains.hms.domain;

import static com.hyperbrains.hms.domain.WardCoverTestSamples.*;
import static com.hyperbrains.hms.domain.WardTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class WardCoverTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(WardCover.class);
        WardCover wardCover1 = getWardCoverSample1();
        WardCover wardCover2 = new WardCover();
        assertThat(wardCover1).isNotEqualTo(wardCover2);

        wardCover2.setId(wardCover1.getId());
        assertThat(wardCover1).isEqualTo(wardCover2);

        wardCover2 = getWardCoverSample2();
        assertThat(wardCover1).isNotEqualTo(wardCover2);
    }

    @Test
    void wardTest() {
        WardCover wardCover = getWardCoverRandomSampleGenerator();
        Ward wardBack = getWardRandomSampleGenerator();

        wardCover.setWard(wardBack);
        assertThat(wardCover.getWard()).isEqualTo(wardBack);

        wardCover.ward(null);
        assertThat(wardCover.getWard()).isNull();
    }
}
