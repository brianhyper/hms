package com.hyperbrains.hms.service.rules;

import com.hyperbrains.hms.domain.DispenseLine;
import com.hyperbrains.hms.domain.Drug;
import com.hyperbrains.hms.domain.PrescriptionLine;
import java.math.BigDecimal;

/**
 * The drug as it was, written onto the line that was prescribed or handed over against it.
 *
 * <p>A line points at a catalogue entry, and the catalogue entry can be renamed, repriced or reclassified tomorrow.
 * Without a copy, last month's prescription would then say it was for a drug at a price nobody ever agreed and in a
 * class it was not in at the time — and it would say so silently, because the line would still look internally
 * consistent. The copy is what makes a prescription and a dispense readable as what happened rather than as what the
 * catalogue says today.
 *
 * <p>The classification is stored as its label rather than as a live reference, for the same reason: the history has
 * to keep meaning what it meant when it was written, even if the set of classifications is rearranged later.
 *
 * <p>Written once here rather than twice at the two places lines are created, so that prescribing and dispensing
 * cannot drift apart in what they keep.
 */
public final class DrugSnapshot {

    private DrugSnapshot() {}

    /** What was prescribed: the drug as it stands at the moment the line is created. */
    public static void onto(PrescriptionLine line, Drug drug) {
        line.setDrugName(drug.getName());
        line.setDrugUnit(drug.getUnit());
        line.setDrugPrice(drug.getPrice());
        line.setDrugClassification(classificationOf(drug));
    }

    /** What was handed over: the drug as it stands at the moment of the hand-over. */
    public static void onto(DispenseLine line, Drug drug) {
        line.setDrugName(drug.getName());
        line.setDrugUnit(drug.getUnit());
        line.setDrugPrice(drug.getPrice());
        line.setDrugClassification(classificationOf(drug));
    }

    private static String classificationOf(Drug drug) {
        return drug.getClassification() == null ? null : drug.getClassification().name();
    }

    /**
     * What to show as the drug's name: what the line recorded, falling back to the catalogue entry only where nothing
     * was recorded because the line predates the snapshot.
     *
     * <p>Reading the catalogue entry first is what made the screen wrong: the stored line held the name that was
     * prescribed, and the pharmacy queue showed the name it has since been renamed to. The fallback exists because the
     * columns are deliberately nullable, and a historical line must still show a drug rather than show nothing.
     */
    public static String nameToShow(PrescriptionLine line) {
        return line.getDrugName() != null ? line.getDrugName() : nameOf(line.getDrug());
    }

    public static String unitToShow(PrescriptionLine line) {
        return line.getDrugUnit() != null ? line.getDrugUnit() : unitOf(line.getDrug());
    }

    public static BigDecimal priceToShow(PrescriptionLine line) {
        return line.getDrugPrice() != null ? line.getDrugPrice() : priceOf(line.getDrug());
    }

    public static String nameToShow(DispenseLine line) {
        return line.getDrugName() != null ? line.getDrugName() : nameOf(line.getDrug());
    }

    public static String unitToShow(DispenseLine line) {
        return line.getDrugUnit() != null ? line.getDrugUnit() : unitOf(line.getDrug());
    }

    private static String nameOf(Drug drug) {
        return drug == null ? null : drug.getName();
    }

    private static String unitOf(Drug drug) {
        return drug == null ? null : drug.getUnit();
    }

    private static BigDecimal priceOf(Drug drug) {
        return drug == null ? null : drug.getPrice();
    }
}
