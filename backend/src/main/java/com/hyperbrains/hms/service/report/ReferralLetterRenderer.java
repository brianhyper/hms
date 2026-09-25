package com.hyperbrains.hms.service.report;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

/**
 * Renders a referral letter as a PDF.
 *
 * <p>Deliberately takes plain strings rather than entities: a letter is a presentation concern, and
 * keeping it that way means the layout can be tested without a database and the workflow service can
 * change what it sends without changing how it looks.
 *
 * <p><strong>Character set.</strong> The standard PDF fonts cover WinAnsi, which is Latin-1 plus a few
 * punctuation marks — not the whole of Unicode. Characters outside it (a name like "Ngũgĩ", or any
 * non-Latin script) cannot be drawn by this font at all, and PDFBox throws rather than rendering
 * garbage. They are therefore folded to their closest Latin equivalent where one exists, and replaced
 * with {@code ?} otherwise. Rendering a fully Unicode letter needs an embedded TrueType font, which is
 * a deliberate future change rather than something to fake here.
 */
public class ReferralLetterRenderer {

    private static final float MARGIN = 56f;

    private static final float LEADING = 15f;

    private static final float BODY_SIZE = 11f;

    private static final float TITLE_SIZE = 17f;

    private static final char UNREPRESENTABLE = '?';

    private final String facilityName;

    public ReferralLetterRenderer(String facilityName) {
        this.facilityName = facilityName;
    }

    /** Everything the letter shows. Dates and codes arrive pre-formatted. */
    public record ReferralLetter(
        String patientName,
        String hospitalId,
        String sex,
        String dateOfBirth,
        String destination,
        String department,
        String reason,
        String notes,
        String referredBy,
        String issuedOn
    ) {}

    public byte[] render(ReferralLetter letter) {
        try (PDDocument document = new PDDocument()) {
            PDFont regular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDFont bold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

            PageWriter writer = new PageWriter(document, regular);
            writer.writeBold(bold, facilityName, TITLE_SIZE);
            writer.write(bold, "Referral Letter", 12f);
            writer.blank();

            writer.write(bold, "Patient", BODY_SIZE);
            writer.write(regular, letter.hospitalId() + "  " + letter.patientName(), BODY_SIZE);
            writer.write(regular, describePerson(letter), BODY_SIZE);
            writer.blank();

            writer.write(bold, "Referred to", BODY_SIZE);
            writer.write(regular, letter.destination(), BODY_SIZE);
            if (isPresent(letter.department())) {
                writer.write(regular, "Department: " + letter.department(), BODY_SIZE);
            }
            writer.blank();

            writer.write(bold, "Reason for referral", BODY_SIZE);
            writer.writeParagraph(regular, letter.reason(), BODY_SIZE);

            if (isPresent(letter.notes())) {
                writer.blank();
                writer.write(bold, "Additional notes", BODY_SIZE);
                writer.writeParagraph(regular, letter.notes(), BODY_SIZE);
            }

            writer.blank();
            writer.write(regular, "Referred by " + letter.referredBy() + " on " + letter.issuedOn, BODY_SIZE);

            writer.close();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            // The caller is producing a document for a patient; there is no meaningful way to continue.
            throw new UncheckedIOException("Failed to render the referral letter", e);
        }
    }

    private static String describePerson(ReferralLetter letter) {
        List<String> parts = new ArrayList<>(2);
        if (isPresent(letter.dateOfBirth())) {
            parts.add("Date of birth: " + letter.dateOfBirth());
        }
        if (isPresent(letter.sex())) {
            parts.add("Sex: " + letter.sex());
        }
        return String.join("   ", parts);
    }

    private static boolean isPresent(String value) {
        return value != null && !value.isBlank();
    }

    /** Writes flowing text a page at a time, starting a new page rather than running off the bottom. */
    private static final class PageWriter {

        private final PDDocument document;

        private final PDFont measuringFont;

        private PDPageContentStream stream;

        private float cursorY;

        private PageWriter(PDDocument document, PDFont measuringFont) {
            this.document = document;
            this.measuringFont = measuringFont;
        }

        void write(PDFont font, String text, float size) throws IOException {
            for (String line : wrap(toWinAnsi(text, font), font, size)) {
                writeLine(font, line, size);
            }
        }

        void writeBold(PDFont font, String text, float size) throws IOException {
            write(font, text, size);
        }

        /** Paragraph text: wrapped, and never lost if it runs longer than a page. */
        void writeParagraph(PDFont font, String text, float size) throws IOException {
            write(font, text, size);
        }

        void blank() throws IOException {
            writeLine(measuringFont, "", BODY_SIZE);
        }

        void close() throws IOException {
            if (stream != null) {
                stream.close();
                stream = null;
            }
        }

        private void writeLine(PDFont font, String line, float size) throws IOException {
            if (stream == null || cursorY < MARGIN) {
                newPage();
            }
            stream.beginText();
            stream.setFont(font, size);
            stream.newLineAtOffset(MARGIN, cursorY);
            stream.showText(line);
            stream.endText();
            cursorY -= LEADING;
        }

        private void newPage() throws IOException {
            close();
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            stream = new PDPageContentStream(document, page);
            cursorY = page.getMediaBox().getHeight() - MARGIN;
        }

        private List<String> wrap(String text, PDFont font, float size) throws IOException {
            List<String> lines = new ArrayList<>();
            float available = PDRectangle.A4.getWidth() - (2 * MARGIN);

            for (String paragraph : text.split("\n", -1)) {
                if (paragraph.isBlank()) {
                    lines.add("");
                    continue;
                }
                StringBuilder current = new StringBuilder();
                for (String word : paragraph.trim().split("\\s+")) {
                    String candidate = current.isEmpty() ? word : current + " " + word;
                    if (width(candidate, font, size) <= available || current.isEmpty()) {
                        current.setLength(0);
                        current.append(candidate);
                    } else {
                        lines.add(current.toString());
                        current.setLength(0);
                        current.append(word);
                    }
                }
                lines.add(current.toString());
            }
            return lines;
        }

        private float width(String text, PDFont font, float size) throws IOException {
            return font.getStringWidth(text) / 1000f * size;
        }
    }

    /**
     * Folds text into what the given font can actually draw.
     *
     * <p>Representability is asked of the font rather than guessed from a character range: WinAnsi
     * covers Latin-1 <em>and</em> a set of typographic marks in the 0x80-0x9F block (em dash, curly
     * quotes, ellipsis), and a hand-written range check would silently downgrade punctuation that
     * doctors type all the time.
     *
     * <p>Stripping diacritics turns "Ngũgĩ" into "Ngugi" — not correct, but recognisable, and far
     * better than a letter that refuses to generate. Anything with no Latin approximation at all
     * becomes {@code ?}, because silently rendering a wrong glyph would be worse than a visible gap.
     */
    static String toWinAnsi(String value, PDFont font) {
        if (value == null) {
            return "";
        }
        StringBuilder out = new StringBuilder(value.length());
        for (char c : value.toCharArray()) {
            if (c == '\n' || c == '\t') {
                out.append(c == '\n' ? '\n' : ' ');
            } else if (c < 0x20) {
                continue;
            } else if (isDrawable(font, c)) {
                out.append(c);
            } else {
                out.append(fold(c, font));
            }
        }
        return out.toString();
    }

    /** A single character is drawable when the font can measure it. */
    private static boolean isDrawable(PDFont font, char c) {
        try {
            font.getStringWidth(String.valueOf(c));
            return true;
        } catch (IllegalArgumentException notInThisFont) {
            return false;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to measure text", e);
        }
    }

    /**
     * The closest drawable character, found by decomposing the character and taking the first part the
     * font can draw. {@code ũ} decomposes to {@code u} plus a combining tilde, so it becomes {@code u}.
     */
    private static char fold(char c, PDFont font) {
        for (char candidate : Normalizer.normalize(String.valueOf(c), Normalizer.Form.NFD).toCharArray()) {
            if (isDrawable(font, candidate)) {
                return candidate;
            }
        }
        return UNREPRESENTABLE;
    }
}
