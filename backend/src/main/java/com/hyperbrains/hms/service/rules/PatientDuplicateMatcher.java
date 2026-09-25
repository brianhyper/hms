package com.hyperbrains.hms.service.rules;

import com.hyperbrains.hms.domain.enumeration.IdentityDocumentType;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * Decides whether a patient being registered looks like one already on file.
 *
 * <p>Deliberately a pure class: no Spring, no repository, no clock. All inputs arrive as
 * parameters so every rule below is unit-testable without a database, which matters because this
 * is the logic that decides whether two human beings are treated as the same person.
 *
 * <p>Two genuinely different checks live here, and they must not be conflated:
 * <ul>
 *   <li>{@link #exactIdentityDocumentMatch} — a hard gate. The caller must not save without an
 *       explicit override, because a national ID matching an existing record is strong evidence
 *       of a duplicate.</li>
 *   <li>{@link #possibleDuplicates} — advisory only. It must never block a save: many patients
 *       legitimately have no ID at all (minors, unidentified emergency cases).</li>
 * </ul>
 */
public final class PatientDuplicateMatcher {

    /** Why a record was surfaced. Returned to the client so Reception sees the reasoning. */
    public enum Reason {
        SAME_IDENTITY_DOCUMENT,
        SIMILAR_NAME,
        SAME_PHONE,
        SAME_DATE_OF_BIRTH,
        SIMILAR_AGE,
    }

    /** Tuning, normally sourced from {@code HmsProperties.duplicate}. */
    public record Settings(int nameSimilarityThreshold, int strongNameSimilarity, int ageToleranceYears) {}

    /** The patient about to be created. */
    public record Candidate(
        String fullName,
        LocalDate dateOfBirth,
        Integer estimatedAge,
        String phone,
        IdentityDocumentType identityDocumentType,
        String identityDocumentNumber
    ) {}

    /** A patient already on file, reduced to just the matching-relevant fields. */
    public record ExistingPatient(
        Long id,
        String hospitalId,
        String fullName,
        LocalDate dateOfBirth,
        Integer estimatedAge,
        String phone,
        IdentityDocumentType identityDocumentType,
        String identityDocumentNumber
    ) {}

    public record Match(ExistingPatient patient, int nameSimilarity, Set<Reason> reasons) {}

    /**
     * A candidate is surfaced when it has either <em>two independent</em> matching signals, or one
     * signal strong enough to stand alone. Requiring corroboration is what keeps this useful: a
     * common name alone would otherwise bury Reception in false positives, and a shared phone alone
     * is not decisive because families share numbers.
     */
    private PatientDuplicateMatcher() {}

    /** A hard identity-document match, if one exists. */
    public static Optional<Match> exactIdentityDocumentMatch(Candidate candidate, List<ExistingPatient> existing) {
        if (candidate.identityDocumentType() == null) {
            return Optional.empty();
        }
        String wanted = normalizeDocumentNumber(candidate.identityDocumentNumber());
        if (wanted.isEmpty()) {
            return Optional.empty();
        }
        return existing
            .stream()
            .filter(p -> p.identityDocumentType() == candidate.identityDocumentType())
            .filter(p -> normalizeDocumentNumber(p.identityDocumentNumber()).equals(wanted))
            .findFirst()
            .map(p -> new Match(p, nameSimilarity(candidate.fullName(), p.fullName()), EnumSet.of(Reason.SAME_IDENTITY_DOCUMENT)));
    }

    /**
     * Advisory matches, best first. Never a reason to refuse a save.
     */
    public static List<Match> possibleDuplicates(
        Candidate candidate,
        List<ExistingPatient> existing,
        Settings settings,
        LocalDate today
    ) {
        String candidateName = normalizeName(candidate.fullName());
        String candidatePhone = normalizePhone(candidate.phone());
        List<Match> matches = new ArrayList<>();

        for (ExistingPatient other : existing) {
            Set<Reason> reasons = EnumSet.noneOf(Reason.class);
            String otherName = normalizeName(other.fullName());

            int similarity = nameSimilarity(candidateName, otherName);
            if (similarity >= settings.nameSimilarityThreshold()) {
                reasons.add(Reason.SIMILAR_NAME);
            }
            if (!candidatePhone.isEmpty() && candidatePhone.equals(normalizePhone(other.phone()))) {
                reasons.add(Reason.SAME_PHONE);
            }
            if (candidate.dateOfBirth() != null && candidate.dateOfBirth().equals(other.dateOfBirth())) {
                reasons.add(Reason.SAME_DATE_OF_BIRTH);
            } else if (agesWithinTolerance(candidate, other, settings.ageToleranceYears(), today)) {
                reasons.add(Reason.SIMILAR_AGE);
            }
            if (isSameDocument(candidate, other)) {
                reasons.add(Reason.SAME_IDENTITY_DOCUMENT);
            }

            if (isWorthSurfacing(reasons, similarity, settings)) {
                matches.add(new Match(other, similarity, reasons));
            }
        }

        matches.sort(Comparator.comparingInt(Match::nameSimilarity).reversed());
        return matches;
    }

    private static boolean isWorthSurfacing(Set<Reason> reasons, int similarity, Settings settings) {
        boolean strongOnItsOwn = similarity >= settings.strongNameSimilarity() || reasons.contains(Reason.SAME_IDENTITY_DOCUMENT);
        return strongOnItsOwn || reasons.size() >= 2;
    }

    private static boolean isSameDocument(Candidate candidate, ExistingPatient other) {
        if (candidate.identityDocumentType() == null || candidate.identityDocumentType() != other.identityDocumentType()) {
            return false;
        }
        String a = normalizeDocumentNumber(candidate.identityDocumentNumber());
        return !a.isEmpty() && a.equals(normalizeDocumentNumber(other.identityDocumentNumber()));
    }

    /**
     * Age comparison, used only when at least one side has no date of birth. Estimated ages are
     * approximate by nature, so this is a tolerance band rather than an equality test.
     */
    private static boolean agesWithinTolerance(Candidate candidate, ExistingPatient other, int toleranceYears, LocalDate today) {
        Integer a = effectiveAge(candidate.dateOfBirth(), candidate.estimatedAge(), today);
        Integer b = effectiveAge(other.dateOfBirth(), other.estimatedAge(), today);
        if (a == null || b == null) {
            return false;
        }
        return Math.abs(a - b) <= toleranceYears;
    }

    private static Integer effectiveAge(LocalDate dateOfBirth, Integer estimatedAge, LocalDate today) {
        if (dateOfBirth != null) {
            return Period.between(dateOfBirth, today).getYears();
        }
        return estimatedAge;
    }

    /** Letters and digits only, lower-cased, single-spaced. Punctuation and spacing are noise. */
    public static String normalizeName(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{N}]+", " ").trim();
    }

    /**
     * First normalized name token. Used to pre-filter candidates in SQL; scoring happens in Java.
     */
    public static String leadingNameToken(String raw) {
        String normalized = normalizeName(raw);
        int space = normalized.indexOf(' ');
        return space < 0 ? normalized : normalized.substring(0, space);
    }

    /** Digits only, so {@code +254 712-345 678} and {@code 0712345678} compare consistently. */
    public static String normalizePhone(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replaceAll("\\D+", "");
    }

    /** Upper-cased, alphanumeric only. */
    public static String normalizeDocumentNumber(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.toUpperCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{N}]+", "");
    }

    /** Similarity of two names as a whole number 0-100, based on edit distance. */
    public static int nameSimilarity(String left, String right) {
        String a = normalizeName(left);
        String b = normalizeName(right);
        if (a.isEmpty() && b.isEmpty()) {
            return 100;
        }
        if (a.isEmpty() || b.isEmpty()) {
            return 0;
        }
        if (a.equals(b)) {
            return 100;
        }
        int longest = Math.max(a.length(), b.length());
        int distance = levenshtein(a, b);
        return Math.max(0, 100 - (distance * 100) / longest);
    }

    private static int levenshtein(String a, String b) {
        int[] previous = new int[b.length() + 1];
        int[] current = new int[b.length() + 1];
        for (int j = 0; j <= b.length(); j++) {
            previous[j] = j;
        }
        for (int i = 1; i <= a.length(); i++) {
            current[0] = i;
            for (int j = 1; j <= b.length(); j++) {
                int substitutionCost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                int deletion = previous[j] + 1;
                int insertion = current[j - 1] + 1;
                int substitution = previous[j - 1] + substitutionCost;
                current[j] = Math.min(deletion, Math.min(insertion, substitution));
            }
            int[] swap = previous;
            previous = current;
            current = swap;
        }
        return previous[b.length()];
    }
}
