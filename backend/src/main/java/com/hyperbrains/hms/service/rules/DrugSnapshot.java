package com.hyperbrains.hms.service.rules;

import com.hyperbrains.hms.domain.DispenseLine;
import com.hyperbrains.hms.domain.Drug;
import com.hyperbrains.hms.domain.PrescriptionLine;

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
}
