package com.hyperbrains.hms.service.report;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

/**
 * The referral letter.
 *
 * <p>The bytes are parsed back with a real PDF reader rather than inspected as text, because the only
 * thing that matters about a generated document is that a receiving clinician's viewer can open it. A
 * test that grepped the raw bytes would pass on a structurally broken file that compresses its
 * streams.
 */
class ReferralLetterRendererTest {

    private static final String FACILITY = "Nairobi Regional Hospital";

    private final ReferralLetterRenderer renderer = new ReferralLetterRenderer(FACILITY);

    private final PDFont helvetica = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

    @Test
    void theLetterIsAPdfThatOpens() throws IOException {
        byte[] pdf = renderer.render(letter("Sore throat", null, "External Clinic"));

        assertThat(new String(pdf, 0, 5)).isEqualTo("%PDF-");
        try (PDDocument document = Loader.loadPDF(pdf)) {
            assertThat(document.getNumberOfPages()).isEqualTo(1);
        }
    }

    @Test
    void theLetterCarriesTheLetterheadAndThePatient() throws IOException {
        String text = textOf(renderer.render(letter("Sore throat", null, "External Clinic")));

        assertThat(text).contains(FACILITY);
        assertThat(text).contains("Referral Letter");
        // Identified by hospital number as well as name: the receiving facility has no other way to be
        // sure whose patient this was.
        assertThat(text).contains("HMS-2026-0042");
        assertThat(text).contains("Wanjiku Kamau");
        assertThat(text).contains("Date of birth: 1990-05-01");
    }

    @Test
    void theLetterCarriesTheDestinationTheReasonAndWhoReferredIt() throws IOException {
        String text = textOf(renderer.render(letter("Suspected pulmonary tuberculosis", "Patient is a smoker", "Kenyatta National")));

        assertThat(text).contains("Kenyatta National");
        assertThat(text).contains("Suspected pulmonary tuberculosis");
        assertThat(text).contains("Patient is a smoker");
        assertThat(text).contains("dr.odeyo");
        assertThat(text).contains("1 September 2026");
    }

    /** A referral written in a hurry may have no notes and no department; it must still produce a letter. */
    @Test
    void aSparseReferralStillRenders() throws IOException {
        ReferralLetterRenderer.ReferralLetter sparse = new ReferralLetterRenderer.ReferralLetter(
            "Unknown Patient",
            "HMS-2026-0001",
            null,
            null,
            "Outside",
            null,
            "Needs review",
            null,
            "dr.odeyo",
            "1 September 2026"
        );

        String text = textOf(renderer.render(sparse));

        assertThat(text).contains("Unknown Patient");
        assertThat(text).doesNotContain("Additional notes");
        assertThat(text).doesNotContain("Sex:");
    }

    /** A long reason has to paginate rather than run off the bottom of the page. */
    @Test
    void aLongReasonSpillsOntoAnotherPage() throws IOException {
        // Long enough to need several pages: the point is that pagination happens at all, and that the
        // tail of the reason survives it.
        String reason = ("The patient reports a persistent cough. ").repeat(250);

        byte[] pdf = renderer.render(letter(reason, null, "External Clinic"));

        try (PDDocument document = Loader.loadPDF(pdf)) {
            assertThat(document.getNumberOfPages()).isGreaterThan(1);
        }
        // And nothing was silently dropped at the page boundary.
        assertThat(textOf(pdf)).contains("persistent cough.");
    }

    @Test
    void charactersTheStandardFontCannotDrawAreFoldedRatherThanDropped() {
        assertThat(ReferralLetterRenderer.toWinAnsi("José", helvetica)).isEqualTo("José");
        // Names with a diacritic the standard font lacks are folded to their base letter: wrong, but
        // recognisable, and better than refusing to produce the letter.
        assertThat(ReferralLetterRenderer.toWinAnsi("Ngũgĩ", helvetica)).isEqualTo("Ngugi");
        // Typographic punctuation the font does support must survive rather than become a question mark.
        assertThat(ReferralLetterRenderer.toWinAnsi("a — b “c”", helvetica)).isEqualTo("a — b “c”");
        // Nothing Latin to fall back on is replaced outright, so nobody gets a silently wrong glyph.
        // "Кириллица" is nine characters, so there are nine replacements and not one fewer.
        assertThat(ReferralLetterRenderer.toWinAnsi("Кириллица", helvetica)).isEqualTo("?????????");
        assertThat(ReferralLetterRenderer.toWinAnsi(null, helvetica)).isEmpty();
        assertThat(ReferralLetterRenderer.toWinAnsi("a\nb\tc", helvetica)).isEqualTo("a\nb c");
    }

    private static String textOf(byte[] pdf) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdf)) {
            return new PDFTextStripper().getText(document);
        }
    }

    private static ReferralLetterRenderer.ReferralLetter letter(String reason, String notes, String destination) {
        return new ReferralLetterRenderer.ReferralLetter(
            "Wanjiku Kamau",
            "HMS-2026-0042",
            "FEMALE",
            "1990-05-01",
            destination,
            "Respiratory Medicine",
            reason,
            notes,
            "dr.odeyo",
            "1 September 2026"
        );
    }
}
