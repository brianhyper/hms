package com.hyperbrains.hms.domain;

import static com.hyperbrains.hms.domain.DiagnosticOrderTestSamples.*;
import static com.hyperbrains.hms.domain.ResultTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ResultTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Result.class);
        Result result1 = getResultSample1();
        Result result2 = new Result();
        assertThat(result1).isNotEqualTo(result2);

        result2.setId(result1.getId());
        assertThat(result1).isEqualTo(result2);

        result2 = getResultSample2();
        assertThat(result1).isNotEqualTo(result2);
    }

    @Test
    void orderTest() {
        Result result = getResultRandomSampleGenerator();
        DiagnosticOrder diagnosticOrderBack = getDiagnosticOrderRandomSampleGenerator();

        result.setOrder(diagnosticOrderBack);
        assertThat(result.getOrder()).isEqualTo(diagnosticOrderBack);
        assertThat(diagnosticOrderBack.getResult()).isEqualTo(result);

        result.order(null);
        assertThat(result.getOrder()).isNull();
        assertThat(diagnosticOrderBack.getResult()).isNull();
    }
}
