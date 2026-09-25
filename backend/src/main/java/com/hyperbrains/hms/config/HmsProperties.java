package com.hyperbrains.hms.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.time.Duration;

/**
 * HMS-specific configuration.
 *
 * <p>This is the single place for operational policy that clinicians or administrators may want to
 * tune <em>without</em> a code change or a redeploy:
 * <ul>
 *   <li>{@link Ids} — the hospital identifier formats.</li>
 *   <li>{@link Duplicate} — how aggressive the soft patient-duplicate check is.</li>
 *   <li>{@link Vitals} — the two-tier vital-signs thresholds.</li>
 *   <li>{@link Billing} — which catalogue entry prices a general consultation.</li>
 *   <li>{@link Referral} — the letterhead on a referral letter.</li>
 * </ul>
 *
 * <p>Bound from the {@code hms:} block at the bottom of {@code config/application.yml}.
 * {@code ignoreUnknownFields = false} means a typo in that block fails startup instead of being
 * silently ignored — which matters because a wrong threshold is a clinical-safety issue.
 */
@ConfigurationProperties(prefix = "hms", ignoreUnknownFields = false)
public class HmsProperties {

    private final Ids ids = new Ids();

    private final Duplicate duplicate = new Duplicate();

    private final Vitals vitals = new Vitals();

    private final Billing billing = new Billing();

    private final Appointments appointments = new Appointments();

    private final Referral referral = new Referral();

    public Ids getIds() {
        return ids;
    }

    public Duplicate getDuplicate() {
        return duplicate;
    }

    public Vitals getVitals() {
        return vitals;
    }

    public Billing getBilling() {
        return billing;
    }

    public Appointments getAppointments() {
        return appointments;
    }

    public Referral getReferral() {
        return referral;
    }

    /**
     * Referral letter settings.
     *
     * <p>The letterhead is the one part of a referral letter that differs per deployment, and a letter
     * that leaves the building on the wrong letterhead is worse than no letterhead at all — so it is
     * configuration rather than a constant baked into the renderer.
     */
    public static class Referral {

        /** Printed as the letterhead. Set this to the real facility name before go-live. */
        private String facilityName = "Hospital";

        public String getFacilityName() {
            return facilityName;
        }

        public void setFacilityName(String facilityName) {
            this.facilityName = facilityName;
        }
    }

    /**
     * Hospital identifier formats. The change requested most often is {@code digits}, so it is a
     * property rather than a constant.
     */
    public static class Ids {

        /** Prefix for a permanent hospital identifier. */
        private String permanentPrefix = "HMS";

        /** Prefix for the temporary identifier given to an unidentified emergency patient. */
        private String temporaryPrefix = "UNK";

        /** Zero-padded width of the sequence part: 4 renders {@code HMS-2026-0007}. */
        private int digits = 4;

        public String getPermanentPrefix() {
            return permanentPrefix;
        }

        public void setPermanentPrefix(String permanentPrefix) {
            this.permanentPrefix = permanentPrefix;
        }

        public String getTemporaryPrefix() {
            return temporaryPrefix;
        }

        public void setTemporaryPrefix(String temporaryPrefix) {
            this.temporaryPrefix = temporaryPrefix;
        }

        public int getDigits() {
            return digits;
        }

        public void setDigits(int digits) {
            this.digits = digits;
        }
    }

    /**
     * Tuning for the "possible duplicate" check.
     *
     * <p>This check must never block a save — many patients legitimately lack an ID (minors,
     * unidentified emergency cases). These values only control how eagerly a match is surfaced.
     */
    public static class Duplicate {

        /** Name similarity (0-100) at or above which two names count as "similar". */
        private int nameSimilarityThreshold = 80;

        /**
         * Name similarity (0-100) that is strong enough on its own, with no corroborating signal.
         * Guards against missing a genuine duplicate when the phone number was mistyped.
         */
        private int strongNameSimilarity = 95;

        /** Age tolerance in years, used only when a date of birth is missing on either side. */
        private int ageToleranceYears = 2;

        public int getNameSimilarityThreshold() {
            return nameSimilarityThreshold;
        }

        public void setNameSimilarityThreshold(int nameSimilarityThreshold) {
            this.nameSimilarityThreshold = nameSimilarityThreshold;
        }

        public int getStrongNameSimilarity() {
            return strongNameSimilarity;
        }

        public void setStrongNameSimilarity(int strongNameSimilarity) {
            this.strongNameSimilarity = strongNameSimilarity;
        }

        public int getAgeToleranceYears() {
            return ageToleranceYears;
        }

        public void setAgeToleranceYears(int ageToleranceYears) {
            this.ageToleranceYears = ageToleranceYears;
        }
    }

    /**
     * Two-tier vital-signs thresholds, per field.
     *
     * <p>{@code warn*} is the healthy range: a value outside it is <em>saved</em> and returned with
     * a warning. {@code hard*} is the physiologically-possible range: a value outside it is
     * <em>rejected</em>, because it is almost certainly a data-entry mistake rather than a reading.
     * The two must not be collapsed into one generic validation error.
     */
    public static class Vitals {

        private Range temperature = Range.of(36.1, 37.2, 30.0, 45.0);

        private Range pulseRate = Range.of(60, 100, 20, 300);

        private Range systolicBp = Range.of(90, 120, 40, 400);

        private Range diastolicBp = Range.of(60, 80, 20, 300);

        private Range oxygenSaturation = Range.of(95, 100, 50, 100);

        /** Weight has no useful "normal range" to warn about, so hard limits only. */
        private Range weight = Range.hardOnly(0.5, 500.0);

        /** Height (cm). Hard limits only, for the same reason as weight. */
        private Range height = Range.hardOnly(10.0, 260.0);

        public Range getTemperature() {
            return temperature;
        }

        public void setTemperature(Range temperature) {
            this.temperature = temperature;
        }

        public Range getPulseRate() {
            return pulseRate;
        }

        public void setPulseRate(Range pulseRate) {
            this.pulseRate = pulseRate;
        }

        public Range getSystolicBp() {
            return systolicBp;
        }

        public void setSystolicBp(Range systolicBp) {
            this.systolicBp = systolicBp;
        }

        public Range getDiastolicBp() {
            return diastolicBp;
        }

        public void setDiastolicBp(Range diastolicBp) {
            this.diastolicBp = diastolicBp;
        }

        public Range getOxygenSaturation() {
            return oxygenSaturation;
        }

        public void setOxygenSaturation(Range oxygenSaturation) {
            this.oxygenSaturation = oxygenSaturation;
        }

        public Range getWeight() {
            return weight;
        }

        public void setWeight(Range weight) {
            this.weight = weight;
        }

        public Range getHeight() {
            return height;
        }

        public void setHeight(Range height) {
            this.height = height;
        }
    }

    /** A warn band inside a wider hard band. Any bound may be omitted. */
    public static class Range {

        private Double warnMin;

        private Double warnMax;

        private Double hardMin;

        private Double hardMax;

        public static Range of(double warnMin, double warnMax, double hardMin, double hardMax) {
            Range range = new Range();
            range.warnMin = warnMin;
            range.warnMax = warnMax;
            range.hardMin = hardMin;
            range.hardMax = hardMax;
            return range;
        }

        public static Range hardOnly(double hardMin, double hardMax) {
            Range range = new Range();
            range.hardMin = hardMin;
            range.hardMax = hardMax;
            return range;
        }

        public Double getWarnMin() {
            return warnMin;
        }

        public void setWarnMin(Double warnMin) {
            this.warnMin = warnMin;
        }

        public Double getWarnMax() {
            return warnMax;
        }

        public void setWarnMax(Double warnMax) {
            this.warnMax = warnMax;
        }

        public Double getHardMin() {
            return hardMin;
        }

        public void setHardMin(Double hardMin) {
            this.hardMin = hardMin;
        }

        public Double getHardMax() {
            return hardMax;
        }

        public void setHardMax(Double hardMax) {
            this.hardMax = hardMax;
        }
    }

    /** Billing lookups. */
    public static class Billing {

        /**
         * {@code HospitalService.code} of the entry that prices a general consultation.
         *
         * <p>A stable code rather than the display name, so renaming the service cannot silently
         * change what patients are charged.
         */
        private String consultationServiceCode = "CONSULTATION";

        public String getConsultationServiceCode() {
            return consultationServiceCode;
        }

        public void setConsultationServiceCode(String consultationServiceCode) {
            this.consultationServiceCode = consultationServiceCode;
        }
    }

    /** Waiting-list policy. */
    public static class Appointments {

        /**
         * Zone used to decide whether a scheduled appointment has been missed. Appointment date
         * and time are stored as local wall-clock values with no offset, so "has this appointment
         * passed?" is unanswerable without knowing which zone they are in. Defaults to the
         * hospital's own zone rather than UTC, or an 09:00 appointment in Nairobi would look
         * overdue five hours before it started.
         */
        private String zone = "Africa/Nairobi";

        /**
         * Grace period after the scheduled time before a patient counts as a no-show. Someone
         * arriving twenty minutes late has not failed to attend, and marking them as such would
         * stop Reception checking them in at all.
         */
        private int noShowGraceMinutes = 120;

        /** How often to look for missed appointments. */
        private Duration noShowScanInterval = Duration.ofMinutes(15);

        public String getZone() {
            return zone;
        }

        public void setZone(String zone) {
            this.zone = zone;
        }

        public int getNoShowGraceMinutes() {
            return noShowGraceMinutes;
        }

        public void setNoShowGraceMinutes(int noShowGraceMinutes) {
            this.noShowGraceMinutes = noShowGraceMinutes;
        }

        public Duration getNoShowScanInterval() {
            return noShowScanInterval;
        }

        public void setNoShowScanInterval(Duration noShowScanInterval) {
            this.noShowScanInterval = noShowScanInterval;
        }
    }
}
