package com.hyperbrains.hms.config;

import static com.hyperbrains.hms.security.AuthoritiesConstants.*;
import static org.springframework.security.config.Customizer.withDefaults;

import com.hyperbrains.hms.security.*;
import com.hyperbrains.hms.web.filter.SpaWebFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import tech.jhipster.config.JHipsterProperties;

@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfiguration {

    private final JHipsterProperties jHipsterProperties;

    public SecurityConfiguration(JHipsterProperties jHipsterProperties) {
        this.jHipsterProperties = jHipsterProperties;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http.cors(withDefaults())
            .csrf(csrf -> csrf.disable())
            .addFilterAfter(new SpaWebFilter(), BasicAuthenticationFilter.class)
            .headers(headers ->
                headers
                    .contentSecurityPolicy(csp -> csp.policyDirectives(jHipsterProperties.getSecurity().getContentSecurityPolicy()))
                    .frameOptions(FrameOptionsConfig::sameOrigin)
                    .referrerPolicy(referrer -> referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                    .permissionsPolicyHeader(permissions ->
                        permissions.policy(
                            "camera=(), fullscreen=(self), geolocation=(), gyroscope=(), magnetometer=(), microphone=(), midi=(), payment=(), sync-xhr=()"
                        )
                    )
            )
            .authorizeHttpRequests(authz ->
                // prettier-ignore
                authz
                    // ===============================================================
                    // kumbuka kuchange to Public via https juu ya cloudfalre
                    // ===============================================================
                    .requestMatchers("/index.html", "/*.js", "/*.txt", "/*.json", "/*.map", "/*.css").permitAll()
                    .requestMatchers("/*.ico", "/*.png", "/*.svg", "/*.webapp").permitAll()
                    .requestMatchers("/app/**").permitAll()
                    .requestMatchers("/i18n/**").permitAll()
                    .requestMatchers("/content/**").permitAll()
                    .requestMatchers("/swagger-ui/**").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/authenticate").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/authenticate").permitAll()
                    .requestMatchers("/api/register").permitAll()
                    .requestMatchers("/api/activate").permitAll()
                    .requestMatchers("/api/account/reset-password/init").permitAll()
                    .requestMatchers("/api/account/reset-password/finish").permitAll()

                    // ===============================================================
                    // Platform admin
                    // ===============================================================
                    .requestMatchers("/api/admin/**").hasAuthority(ADMIN)
                    .requestMatchers("/v3/api-docs/**").hasAuthority(ADMIN)
                    .requestMatchers("/management/health").permitAll()
                    .requestMatchers("/management/health/**").permitAll()
                    .requestMatchers("/management/info").permitAll()
                    .requestMatchers("/management/prometheus").permitAll()
                    .requestMatchers("/management/**").hasAuthority(ADMIN)

                    // ===============================================================
                    // PHASE 1 RBAC TABLE
                    //
                    // This is the single place where a role is mapped to an endpoint.
                    // Order matters: Spring Security uses the FIRST matching rule, so
                    // every entity rule must appear above the "/api/**" catch-all.
                    //
                    // Scope of this table: WHO may reach an endpoint. It deliberately
                    // does NOT cover WHAT they may see inside the response:
                    //   * a Lab user must only ever see OrderType.LAB rows, never
                    //     radiology  -> enforced by a filtered repository query;
                    //   * Finance must see a test name and price but no clinical notes
                    //     or diagnosis -> enforced by redacted view DTOs;
                    //   * a Doctor must see only their own queue/patients -> filtered
                    //     by Consultation.doctor.
                    //
                    // Generic write verbs (POST/PUT/PATCH/DELETE) on the status-bearing
                    // entities are still reachable here so the generated integration
                    // tests keep working. Each one is removed as its action endpoint
                    // lands (see phases/phase1.md "Corrections" and the state machine).
                    // ===============================================================

                    // ---- Department (catalogue) ----
                    .requestMatchers(HttpMethod.GET, "/api/departments", "/api/departments/**").hasAnyAuthority(RECEPTION, NURSE, DOCTOR, LAB, RADIOLOGY, PHARMACY, FINANCE, ADMIN, SUPER_ADMIN)
                    .requestMatchers("/api/departments", "/api/departments/**").hasAnyAuthority(ADMIN, SUPER_ADMIN)

                    // ---- Diagnosis (catalogue) ----
                    .requestMatchers(HttpMethod.GET, "/api/diagnoses", "/api/diagnoses/**").hasAnyAuthority(RECEPTION, NURSE, DOCTOR, LAB, RADIOLOGY, PHARMACY, FINANCE, ADMIN, SUPER_ADMIN)
                    .requestMatchers("/api/diagnoses", "/api/diagnoses/**").hasAnyAuthority(ADMIN, SUPER_ADMIN)

                    // ---- HospitalService (price catalogue) ----
                    .requestMatchers(HttpMethod.GET, "/api/hospital-services", "/api/hospital-services/**").hasAnyAuthority(RECEPTION, NURSE, DOCTOR, LAB, RADIOLOGY, PHARMACY, FINANCE, ADMIN, SUPER_ADMIN)
                    .requestMatchers("/api/hospital-services", "/api/hospital-services/**").hasAnyAuthority(ADMIN, SUPER_ADMIN)

                    // ---- LabTest (price catalogue) ----
                    .requestMatchers(HttpMethod.GET, "/api/lab-tests", "/api/lab-tests/**").hasAnyAuthority(RECEPTION, NURSE, DOCTOR, LAB, RADIOLOGY, PHARMACY, FINANCE, ADMIN, SUPER_ADMIN)
                    .requestMatchers("/api/lab-tests", "/api/lab-tests/**").hasAnyAuthority(ADMIN, SUPER_ADMIN)

                    // ---- RadiologyExam (price catalogue) ----
                    .requestMatchers(HttpMethod.GET, "/api/radiology-exams", "/api/radiology-exams/**").hasAnyAuthority(RECEPTION, NURSE, DOCTOR, LAB, RADIOLOGY, PHARMACY, FINANCE, ADMIN, SUPER_ADMIN)
                    .requestMatchers("/api/radiology-exams", "/api/radiology-exams/**").hasAnyAuthority(ADMIN, SUPER_ADMIN)

                    // ---- Patient (identifying details are read by every clinical role) ----
                    .requestMatchers(HttpMethod.GET, "/api/patients", "/api/patients/**").hasAnyAuthority(RECEPTION, NURSE, DOCTOR, LAB, RADIOLOGY, PHARMACY, FINANCE, ADMIN, SUPER_ADMIN)
                    // A direct update changes a patient's name or recorded allergies with no reason and no
                    // history. Correcting goes through /api/patient-corrections/{patientId}, which demands a
                    // reason and records which fields changed.
                    .requestMatchers("/api/patients", "/api/patients/**").hasAnyAuthority(SUPER_ADMIN)

                    // ---- Appointment ----
                    .requestMatchers(HttpMethod.GET, "/api/appointments", "/api/appointments/**").hasAnyAuthority(RECEPTION, NURSE, DOCTOR, ADMIN, SUPER_ADMIN)
                    .requestMatchers("/api/appointments", "/api/appointments/**").hasAnyAuthority(RECEPTION, ADMIN, SUPER_ADMIN)

                    // ---- Visit ----
                    .requestMatchers(HttpMethod.GET, "/api/visits", "/api/visits/**").hasAnyAuthority(RECEPTION, NURSE, DOCTOR, LAB, RADIOLOGY, PHARMACY, FINANCE, ADMIN, SUPER_ADMIN)
                    // A direct write can set type and status by hand, which is the whole of what the
                    // admit-patient action does — so leaving it open would make that action, and the
                    // guards on it, optional. Every change of an encounter's direction now goes through
                    // the workflow route that owns it. The super-admin retains the raw endpoint as the
                    // escape hatch, as on every other status-bearing entity.
                    .requestMatchers("/api/visits", "/api/visits/**").hasAnyAuthority(SUPER_ADMIN)

                    // ---- Admission conversion ----
                    // A doctor decides that this patient is staying: it is a clinical judgement about
                    // their condition, and it takes the visit off the outpatient path for good. The desk
                    // is not on this list — it books the patient in, it does not keep them in.
                    .requestMatchers(HttpMethod.POST, "/api/visit-admissions/*/admit").hasAnyAuthority(DOCTOR, ADMIN, SUPER_ADMIN)

                    // ---- VitalSigns ----
                    .requestMatchers(HttpMethod.GET, "/api/vital-signs", "/api/vital-signs/**").hasAnyAuthority(NURSE, DOCTOR, ADMIN, SUPER_ADMIN)
                    .requestMatchers("/api/vital-signs", "/api/vital-signs/**").hasAnyAuthority(NURSE, DOCTOR, ADMIN, SUPER_ADMIN)

                    // ---- Consultation ----
                    .requestMatchers(HttpMethod.GET, "/api/consultations", "/api/consultations/**").hasAnyAuthority(NURSE, DOCTOR, ADMIN, SUPER_ADMIN)
                    .requestMatchers("/api/consultations", "/api/consultations/**").hasAnyAuthority(DOCTOR, ADMIN, SUPER_ADMIN)

                    // ---- DiagnosticOrder ----
                    // Reading these rows is unrestricted enough to leak clinical notes to whoever
                    // asks, so the only readers left are the clinicians treating the patient. The
                    // lab and radiology desks get their work from /api/visit-orders/worklist, which
                    // returns only their own discipline and only identifying patient details.
                    .requestMatchers(HttpMethod.GET, "/api/diagnostic-orders", "/api/diagnostic-orders/**").hasAnyAuthority(NURSE, DOCTOR, ADMIN, SUPER_ADMIN)
                    // Direct writes would create an order that is never charged for and never moves
                    // the visit, because that logic lives in the workflow service. Only the
                    // super-admin retains the raw endpoint, as an escape hatch.
                    .requestMatchers("/api/diagnostic-orders", "/api/diagnostic-orders/**").hasAnyAuthority(SUPER_ADMIN)

                    // ---- Result ----
                    // A result value is the clinical finding itself. It is written through
                    // /api/visit-orders/{id}/result, which also completes the order, charges for it
                    // and recomputes the visit; the raw endpoint circumvents all three.
                    .requestMatchers(HttpMethod.GET, "/api/results", "/api/results/**").hasAnyAuthority(DOCTOR, ADMIN, SUPER_ADMIN)
                    .requestMatchers("/api/results", "/api/results/**").hasAnyAuthority(SUPER_ADMIN)

                    // ---- Referral ----
                    .requestMatchers(HttpMethod.GET, "/api/referrals", "/api/referrals/**").hasAnyAuthority(RECEPTION, NURSE, DOCTOR, ADMIN, SUPER_ADMIN)
                    // A direct write records a referral without ending the local journey, so the visit would
                    // stay waiting on work the referral already decided is not ours to finish. Referring goes
                    // through /api/visit-referrals/{visitId}/create.
                    .requestMatchers("/api/referrals", "/api/referrals/**").hasAnyAuthority(SUPER_ADMIN)

                    // ---- Prescription ----
                    // Finance is deliberately absent: it gets drug, quantity and money from
                    // /api/visit-prescriptions/billable/**, not the posology on this entity.
                    .requestMatchers(HttpMethod.GET, "/api/prescriptions", "/api/prescriptions/**").hasAnyAuthority(NURSE, DOCTOR, PHARMACY, ADMIN, SUPER_ADMIN)
                    // A direct write creates a prescription without reserving stock or charging for it,
                    // which is the one thing this slice exists to guarantee.
                    .requestMatchers("/api/prescriptions", "/api/prescriptions/**").hasAnyAuthority(SUPER_ADMIN)

                    // ---- PrescriptionLine ----
                    .requestMatchers(HttpMethod.GET, "/api/prescription-lines", "/api/prescription-lines/**").hasAnyAuthority(NURSE, DOCTOR, PHARMACY, ADMIN, SUPER_ADMIN)
                    .requestMatchers("/api/prescription-lines", "/api/prescription-lines/**").hasAnyAuthority(SUPER_ADMIN)

                    // ---- Drug (stock) ----
                    // Reading the catalogue is clinical: a prescriber has to see what exists and what
                    // it costs. Finance reads prices through the billable view instead.
                    .requestMatchers(HttpMethod.GET, "/api/drugs", "/api/drugs/**").hasAnyAuthority(NURSE, DOCTOR, PHARMACY, ADMIN, SUPER_ADMIN)
                    // Writes are super-admin only because this endpoint can set reservedStock directly.
                    // Letting a pharmacy write it by hand would silently break an existing reservation,
                    // which is exactly the invariant the reservation system exists to hold. Receiving
                    // deliveries and writing off wastage need their own workflow; until that exists,
                    // hand-editing stock is an administrative act.
                    .requestMatchers("/api/drugs", "/api/drugs/**").hasAnyAuthority(SUPER_ADMIN)

                    // ---- Dispense ----
                    // Dispensing is what reduces real stock, so it must go through the dispensing
                    // workflow rather than a direct write. Finance is absent: a dispense record is a
                    // hand-over, not a price.
                    .requestMatchers(HttpMethod.GET, "/api/dispenses", "/api/dispenses/**").hasAnyAuthority(DOCTOR, PHARMACY, ADMIN, SUPER_ADMIN)
                    .requestMatchers("/api/dispenses", "/api/dispenses/**").hasAnyAuthority(SUPER_ADMIN)

                    // ---- DispenseLine ----
                    .requestMatchers(HttpMethod.GET, "/api/dispense-lines", "/api/dispense-lines/**").hasAnyAuthority(DOCTOR, PHARMACY, ADMIN, SUPER_ADMIN)
                    .requestMatchers("/api/dispense-lines", "/api/dispense-lines/**").hasAnyAuthority(SUPER_ADMIN)

                    // ---- Bill ----
                    .requestMatchers(HttpMethod.GET, "/api/bills", "/api/bills/**").hasAnyAuthority(RECEPTION, FINANCE, ADMIN, SUPER_ADMIN)
                    // A direct write sets totalAmount and status by hand, which would mark a bill paid
                    // without any money arriving and without releasing the medicine that is owed.
                    .requestMatchers("/api/bills", "/api/bills/**").hasAnyAuthority(SUPER_ADMIN)

                    // ---- BillLineItem ----
                    .requestMatchers(HttpMethod.GET, "/api/bill-line-items", "/api/bill-line-items/**").hasAnyAuthority(FINANCE, ADMIN, SUPER_ADMIN)
                    .requestMatchers("/api/bill-line-items", "/api/bill-line-items/**").hasAnyAuthority(FINANCE, ADMIN, SUPER_ADMIN)

                    // ---- Payment ----
                    .requestMatchers(HttpMethod.GET, "/api/payments", "/api/payments/**").hasAnyAuthority(RECEPTION, FINANCE, ADMIN, SUPER_ADMIN)
                    // Recording a payment here would leave the bill unsettled and the visit open, so the
                    // patient would have paid and still be waiting. /api/visit-payments/{id}/pay is the
                    // only route that does the whole job.
                    .requestMatchers("/api/payments", "/api/payments/**").hasAnyAuthority(SUPER_ADMIN)

                    // ---- AuditLog: internal record, admin eyes only, and never writable ----
                    .requestMatchers("/api/audit-logs", "/api/audit-logs/**").hasAnyAuthority(ADMIN, SUPER_ADMIN)

                    // ---- Patient registration actions ----
                    // A pre-save duplicate check is a read: nurses and doctors need it for intake too.
                    .requestMatchers(HttpMethod.POST, "/api/patient-registration/duplicate-check").hasAnyAuthority(RECEPTION, NURSE, DOCTOR, ADMIN, SUPER_ADMIN)
                    // Creating a permanent record is Reception's job.
                    .requestMatchers(HttpMethod.POST, "/api/patient-registration/register").hasAnyAuthority(RECEPTION, ADMIN, SUPER_ADMIN)
                    // Emergency intake is the one path a Nurse or Doctor owns, because the patient
                    // may be unconscious and cannot wait for a registration desk.
                    .requestMatchers(HttpMethod.POST, "/api/patient-registration/emergency-intake").hasAnyAuthority(NURSE, DOCTOR, ADMIN, SUPER_ADMIN)

                    // ---- Visit intake actions ----
                    // Only the desk turns an appointment into a visit; that is the arrival decision.
                    .requestMatchers(HttpMethod.POST, "/api/visit-intake/check-in/**").hasAnyAuthority(RECEPTION, ADMIN, SUPER_ADMIN)
                    // A walk-in or an emergency may be opened by whoever is at the door.
                    .requestMatchers(HttpMethod.POST, "/api/visit-intake/open").hasAnyAuthority(RECEPTION, NURSE, DOCTOR, ADMIN, SUPER_ADMIN)
                    .requestMatchers(HttpMethod.POST, "/api/visit-intake/pharmacy-only").hasAnyAuthority(RECEPTION, PHARMACY, ADMIN, SUPER_ADMIN)

                    // ---- Work queues ----
                    .requestMatchers(HttpMethod.GET, "/api/queues/vitals").hasAnyAuthority(NURSE, DOCTOR, ADMIN, SUPER_ADMIN)
                    .requestMatchers(HttpMethod.GET, "/api/queues/consultation").hasAnyAuthority(DOCTOR, NURSE, ADMIN, SUPER_ADMIN)
                    .requestMatchers(HttpMethod.GET, "/api/queues/active").hasAnyAuthority(RECEPTION, NURSE, DOCTOR, LAB, RADIOLOGY, PHARMACY, FINANCE, ADMIN, SUPER_ADMIN)

                    // ---- Triage ----
                    // A doctor is included because they may be the one triaging an emergency arrival.
                    .requestMatchers("/api/visit-triage/**").hasAnyAuthority(NURSE, DOCTOR, ADMIN, SUPER_ADMIN)

                    // ---- Consultation workflow ----
                    // Reading a patient's notes and addenda is part of reviewing their record, so a
                    // nurse may see them; writing clinical content is the doctor's alone.
                    .requestMatchers(HttpMethod.GET, "/api/visit-consultation/**").hasAnyAuthority(NURSE, DOCTOR, ADMIN, SUPER_ADMIN)
                    .requestMatchers("/api/visit-consultation/**").hasAnyAuthority(DOCTOR, ADMIN, SUPER_ADMIN)

                    // ---- Diagnostic order workflow ----
                    // Ordered before the type-level rules below, because a worklist is not a {"order"}:
                    // what a lab may read is scoped to its own discipline in the query itself, and a
                    // radiology user must not be able to widen that by asking for the other list.
                    .requestMatchers(HttpMethod.GET, "/api/visit-orders/worklist").hasAnyAuthority(LAB, RADIOLOGY, DOCTOR, ADMIN, SUPER_ADMIN)
                    // Finance gets the priced, non-clinical view and nothing else.
                    .requestMatchers(HttpMethod.GET, "/api/visit-orders/billable/**").hasAnyAuthority(FINANCE, ADMIN, SUPER_ADMIN)
                    // Reviewing what was ordered for a visit is part of treating or nursing that patient.
                    .requestMatchers(HttpMethod.GET, "/api/visit-orders/visit/**").hasAnyAuthority(NURSE, DOCTOR, LAB, RADIOLOGY, ADMIN, SUPER_ADMIN)
                    // The result is recorded by whoever performed the test.
                    .requestMatchers(HttpMethod.POST, "/api/visit-orders/*/result").hasAnyAuthority(LAB, RADIOLOGY, ADMIN, SUPER_ADMIN)
                    // Ordering and abandoning a test are clinical decisions.
                    .requestMatchers(HttpMethod.POST, "/api/visit-orders/*/place", "/api/visit-orders/*/cancel").hasAnyAuthority(DOCTOR, ADMIN, SUPER_ADMIN)

                    // ---- Corrections and the audit trail ----
                    // Clinicians are on this route as well as the desk, because the fields they may correct
                    // are not the fields the desk may correct. That split is enforced in the service with a
                    // 403: an endpoint rule cannot express "this role, but not those fields".
                    .requestMatchers(HttpMethod.PUT, "/api/patient-corrections/*")
                    .hasAnyAuthority(RECEPTION, NURSE, DOCTOR, ADMIN, SUPER_ADMIN)
                    // Correction history is clinical review material: it shows what was altered and why, which
                    // is exactly what a treating clinician needs and the desk does not.
                    .requestMatchers(HttpMethod.GET, "/api/record-history/**").hasAnyAuthority(NURSE, DOCTOR, ADMIN, SUPER_ADMIN)

                    // ---- Patient merge ----
                    // Restricted to the desk and the super-admin, which is who the specification assigns to
                    // it. It is the only action that destroys an identity, so it is deliberately not
                    // something every clinical role can reach — and not the plain admin either.
                    .requestMatchers(HttpMethod.POST, "/api/patient-merges").hasAnyAuthority(RECEPTION, SUPER_ADMIN)

                    // ---- Prescribing workflow ----
                    // The dispensing queue is the one view that must not be widened: only pharmacy hands
                    // the medicine over, and it already shows settled prescriptions only.
                    .requestMatchers(HttpMethod.GET, "/api/visit-prescriptions/pharmacy-queue").hasAnyAuthority(PHARMACY, ADMIN, SUPER_ADMIN)
                    // Finance gets the priced, non-clinical view and nothing else.
                    .requestMatchers(HttpMethod.GET, "/api/visit-prescriptions/billable/**").hasAnyAuthority(FINANCE, ADMIN, SUPER_ADMIN)
                    .requestMatchers(HttpMethod.GET, "/api/visit-prescriptions/visit/**").hasAnyAuthority(NURSE, DOCTOR, PHARMACY, ADMIN, SUPER_ADMIN)
                    // A doctor prescribes for their own patient; pharmacy receives a walk-in carrying a
                    // prescription from an outside prescriber, which is the only way that path is reached.
                    .requestMatchers(HttpMethod.POST, "/api/visit-prescriptions/*/place").hasAnyAuthority(DOCTOR, PHARMACY, ADMIN, SUPER_ADMIN)
                    // Withdrawing is open to pharmacy as well as the prescriber, because an outside
                    // prescription has no doctor of ours behind it and someone has to be able to stop it.
                    .requestMatchers(HttpMethod.POST, "/api/visit-prescriptions/*/cancel").hasAnyAuthority(DOCTOR, PHARMACY, ADMIN, SUPER_ADMIN)
                    // There is deliberately no rule for marking a prescription paid: releasing medicine
                    // before the money arrives would defeat the guarantee, so no HTTP route exists for it.

                    // ---- Pharmacy stock movements ----
                    // Receiving deliveries and writing stock off. These exist so that the generated
                    // /api/drugs write could be closed to pharmacy without removing the ability to keep
                    // stock accurate at all.
                    .requestMatchers("/api/pharmacy-stock/**").hasAnyAuthority(PHARMACY, ADMIN, SUPER_ADMIN)

                    // ---- Payment workflow ----
                    // The desk may read a bill: telling a patient what they owe needs no billing authority.
                    .requestMatchers(HttpMethod.GET, "/api/visit-payments/bill/**").hasAnyAuthority(RECEPTION, FINANCE, ADMIN, SUPER_ADMIN)
                    // Taking money is Finance's. This is the only route that settles a bill, closes the
                    // visit and releases medicine to the pharmacy, so it must not be widened.
                    .requestMatchers(HttpMethod.POST, "/api/visit-payments/*/pay").hasAnyAuthority(FINANCE, ADMIN, SUPER_ADMIN)

                    // ---- Dispensing workflow ----
                    // The last step of the guarantee: only a settled prescription reaches here, so this
                    // route cannot be used to hand medicine over for free.
                    .requestMatchers(HttpMethod.POST, "/api/pharmacy-dispense/*/dispense").hasAnyAuthority(PHARMACY, ADMIN, SUPER_ADMIN)
                    .requestMatchers(HttpMethod.GET, "/api/pharmacy-dispense/*/history").hasAnyAuthority(DOCTOR, PHARMACY, ADMIN, SUPER_ADMIN)

                    // ---- Referral workflow ----
                    // The letter is clinical content: it carries the reason for the referral, so the desk is
                    // not on this list even though it may read the referral record itself.
                    .requestMatchers(HttpMethod.GET, "/api/visit-referrals/*/letter").hasAnyAuthority(NURSE, DOCTOR, ADMIN, SUPER_ADMIN)
                    .requestMatchers(HttpMethod.GET, "/api/visit-referrals/visit/**").hasAnyAuthority(RECEPTION, NURSE, DOCTOR, ADMIN, SUPER_ADMIN)
                    // Ending this hospital's clinical involvement is a doctor's decision.
                    .requestMatchers(HttpMethod.POST, "/api/visit-referrals/*/create", "/api/visit-referrals/*/email").hasAnyAuthority(DOCTOR, ADMIN, SUPER_ADMIN)

                    // ===============================================================
                    // Catch-all: any authenticated hospital user (keeps /api/account
                    // working for the stock ROLE_USER so the login flow is unaffected).
                    // ===============================================================
                    .requestMatchers("/api/**").hasAnyAuthority(ADMIN, SUPER_ADMIN, RECEPTION, NURSE, DOCTOR, LAB, RADIOLOGY, PHARMACY, FINANCE, USER)
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exceptions ->
                exceptions
                    .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
                    .accessDeniedHandler(new BearerTokenAccessDeniedHandler())
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(withDefaults()));
        return http.build();
    }
}
