package com.hyperbrains.hms.domain;

import static com.hyperbrains.hms.domain.AdmissionTestSamples.*;
import static com.hyperbrains.hms.domain.AdmissionTransferTestSamples.*;
import static com.hyperbrains.hms.domain.BedTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class AdmissionTransferTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(AdmissionTransfer.class);
        AdmissionTransfer admissionTransfer1 = getAdmissionTransferSample1();
        AdmissionTransfer admissionTransfer2 = new AdmissionTransfer();
        assertThat(admissionTransfer1).isNotEqualTo(admissionTransfer2);

        admissionTransfer2.setId(admissionTransfer1.getId());
        assertThat(admissionTransfer1).isEqualTo(admissionTransfer2);

        admissionTransfer2 = getAdmissionTransferSample2();
        assertThat(admissionTransfer1).isNotEqualTo(admissionTransfer2);
    }

    @Test
    void admissionTest() {
        AdmissionTransfer admissionTransfer = getAdmissionTransferRandomSampleGenerator();
        Admission admissionBack = getAdmissionRandomSampleGenerator();

        admissionTransfer.setAdmission(admissionBack);
        assertThat(admissionTransfer.getAdmission()).isEqualTo(admissionBack);

        admissionTransfer.admission(null);
        assertThat(admissionTransfer.getAdmission()).isNull();
    }

    @Test
    void fromBedTest() {
        AdmissionTransfer admissionTransfer = getAdmissionTransferRandomSampleGenerator();
        Bed bedBack = getBedRandomSampleGenerator();

        admissionTransfer.setFromBed(bedBack);
        assertThat(admissionTransfer.getFromBed()).isEqualTo(bedBack);

        admissionTransfer.fromBed(null);
        assertThat(admissionTransfer.getFromBed()).isNull();
    }

    @Test
    void toBedTest() {
        AdmissionTransfer admissionTransfer = getAdmissionTransferRandomSampleGenerator();
        Bed bedBack = getBedRandomSampleGenerator();

        admissionTransfer.setToBed(bedBack);
        assertThat(admissionTransfer.getToBed()).isEqualTo(bedBack);

        admissionTransfer.toBed(null);
        assertThat(admissionTransfer.getToBed()).isNull();
    }
}
