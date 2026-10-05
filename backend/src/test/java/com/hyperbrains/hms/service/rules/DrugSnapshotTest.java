package com.hyperbrains.hms.service.rules;

import static org.assertj.core.api.Assertions.assertThat;

import com.hyperbrains.hms.domain.DispenseLine;
import com.hyperbrains.hms.domain.Drug;
import com.hyperbrains.hms.domain.PrescriptionLine;
import com.hyperbrains.hms.domain.enumeration.DrugClassification;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/**
 * The drug as it was, versus the drug as the catalogue says it is now.
 *
 * <p>Each test changes the drug <em>after</em> the line was written, the way a catalogue is really changed — renamed,
 * repriced, reclassified — and then asks the line what happened. Without the copy, every one of these would answer
 * with today's values, and the record would quietly say that a patient was prescribed something at a price and in a
 * class that were never true. That is the failure worth a test, because it is invisible: the line would still add up.
 */
class DrugSnapshotTest {

    @Test
    void aLineKeepsWhatWasPrescribedWhenTheCatalogueChangesAfterwards() {
        Drug drug = drug("Paracetamol", "tablet", "500.00", DrugClassification.OTC);
        PrescriptionLine line = new PrescriptionLine();

        DrugSnapshot.onto(line, drug);

        renameRepriceAndReclassify(drug);

        assertThat(line.getDrugName()).as("the name that was prescribed").isEqualTo("Paracetamol");
        assertThat(line.getDrugUnit()).as("the unit that was prescribed").isEqualTo("tablet");
        assertThat(line.getDrugPrice()).as("the price that was prescribed").isEqualByComparingTo("500.00");
        assertThat(line.getDrugClassification()).as("the class that was prescribed").isEqualTo("OTC");
    }

    @Test
    void aDispenseLineKeepsWhatWasHandedOver() {
        Drug drug = drug("Amoxicillin", "capsule", "120.00", DrugClassification.POM);
        DispenseLine line = new DispenseLine();

        DrugSnapshot.onto(line, drug);

        renameRepriceAndReclassify(drug);

        assertThat(line.getDrugName()).isEqualTo("Amoxicillin");
        assertThat(line.getDrugUnit()).isEqualTo("capsule");
        assertThat(line.getDrugPrice()).isEqualByComparingTo("120.00");
        assertThat(line.getDrugClassification()).isEqualTo("POM");
    }

    /**
     * The classification is kept as its label rather than as a reference to the enumeration, so that a line written
     * today still reads the same if the set of classifications is rearranged tomorrow. This asserts the label, and
     * the field's type is what makes it a label rather than a pointer.
     */
    @Test
    void theClassificationIsKeptAsALabel() {
        Drug drug = drug("Morphine", "ampoule", "900.00", DrugClassification.CONTROLLED);
        PrescriptionLine line = new PrescriptionLine();

        DrugSnapshot.onto(line, drug);

        assertThat(line.getDrugClassification()).isEqualTo(DrugClassification.CONTROLLED.name());
    }

    @Test
    void aDrugWithNoClassificationSnapshotsAsNothingRatherThanFailing() {
        Drug drug = drug("Unclassified", "ml", "10.00", null);
        PrescriptionLine line = new PrescriptionLine();

        DrugSnapshot.onto(line, drug);

        assertThat(line.getDrugName()).isEqualTo("Unclassified");
        assertThat(line.getDrugClassification()).isNull();
    }

    private void renameRepriceAndReclassify(Drug drug) {
        drug.setName(drug.getName() + " Extra");
        drug.setUnit("sachet");
        drug.setPrice(new BigDecimal("9999.99"));
        drug.setClassification(DrugClassification.CONTROLLED);
    }

    private Drug drug(String name, String unit, String price, DrugClassification classification) {
        Drug drug = new Drug();
        drug.setName(name);
        drug.setUnit(unit);
        drug.setPrice(new BigDecimal(price));
        drug.setClassification(classification);
        return drug;
    }
}
