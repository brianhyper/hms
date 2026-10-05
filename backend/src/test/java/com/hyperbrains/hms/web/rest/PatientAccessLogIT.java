package com.hyperbrains.hms.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyperbrains.hms.IntegrationTest;
import com.hyperbrains.hms.domain.Patient;
import com.hyperbrains.hms.domain.enumeration.RegistrationStatus;
import com.hyperbrains.hms.domain.enumeration.Sex;
import com.hyperbrains.hms.repository.AuthorityRepository;
import com.hyperbrains.hms.repository.PatientAccessLogRepository;
import com.hyperbrains.hms.repository.PatientRepository;
import com.hyperbrains.hms.repository.UserRepository;
import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.security.AuthoritiesConstants;
import com.hyperbrains.hms.service.HospitalIdService;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * Chart-open access logging.
 *
 * <p>Four claims, and the last three are the ones that make the first one worth anything: opening a chart is
 * recorded; a request for a patient who is not there is not recorded; a request that never got in at all is not
 * recorded; and no role can write to the log or read it who should not.
 *
 * <p>A log that records attempts as access is worse than no log, because what it accuses someone of is invisible to
 * them and impossible to rebut.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Transactional
class PatientAccessLogIT {

    private static final String PASSWORD = "chart-open-password-1";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PatientAccessLogRepository patientAccessLogRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private HospitalIdService hospitalIdService;

    @Autowired
    private ObjectMapper om;

    @Test
    void openingAChartIsRecordedAgainstThePersonWhoOpenedIt() throws Exception {
        Patient patient = patient("opened");
        User doctor = account("doctor", AuthoritiesConstants.DOCTOR);

        mockMvc
            .perform(get("/api/patients/" + patient.getId()).header(HttpHeaders.AUTHORIZATION, "Bearer " + signIn(doctor)))
            .andExpect(status().isOk());

        var entries = patientAccessLogRepository.findByPatientIdOrderByAccessedAtDesc(patient.getId(), org.springframework.data.domain.PageRequest.of(0, 10));
        assertThat(entries.getContent()).as("one opening, one entry").hasSize(1);
        assertThat(entries.getContent().get(0).getActorLogin()).isEqualTo(doctor.getLogin());
        assertThat(entries.getContent().get(0).getAction()).isEqualTo("VIEW");
        assertThat(entries.getContent().get(0).getAccessedAt()).as("and when").isNotNull();
    }

    @Test
    void askingForAPatientWhoIsNotThereRecordsNothing() throws Exception {
        User doctor = account("doctor", AuthoritiesConstants.DOCTOR);

        mockMvc
            .perform(get("/api/patients/999999999").header(HttpHeaders.AUTHORIZATION, "Bearer " + signIn(doctor)))
            .andExpect(status().isNotFound());

        assertThat(patientAccessLogRepository.countByPatientId(999999999L)).as("nothing to be mistaken for access").isZero();
    }

    @Test
    void aRequestThatNeverGotInRecordsNothing() throws Exception {
        Patient patient = patient("refused");

        // No credentials at all: refused before the route is reached, which is the case a log must not confuse with
        // somebody having looked at the record.
        mockMvc.perform(get("/api/patients/" + patient.getId())).andExpect(status().isUnauthorized());

        // Scoped to this patient, because other suites open charts of their own and a shared database is no place for
        // a global count.
        assertThat(patientAccessLogRepository.countByPatientId(patient.getId())).isZero();
    }

    @Test
    void administrationAndSuperAdminReadItAndNobodyElseDoes() throws Exception {
        Patient patient = patient("readers");
        User doctor = account("doctor", AuthoritiesConstants.DOCTOR);
        String doctorToken = signIn(doctor);
        mockMvc.perform(get("/api/patients/" + patient.getId()).header(HttpHeaders.AUTHORIZATION, "Bearer " + doctorToken));

        assertThat(entriesOf(patient, doctorToken)).as("a clinician has no reason to browse who looked at whom").isEqualTo(403);
        assertThat(entriesOf(patient, signIn(account("admin", AuthoritiesConstants.ADMIN)))).isEqualTo(200);
        assertThat(entriesOf(patient, signIn(account("super", AuthoritiesConstants.SUPER_ADMIN)))).isEqualTo(200);
    }

    /** There is no route that writes an entry: the act of opening a chart is what writes one. */
    @Test
    void theLogCannotBeWrittenOverHttp() throws Exception {
        Patient patient = patient("unwritable");
        String adminToken = signIn(account("admin", AuthoritiesConstants.ADMIN));

        mockMvc
            .perform(
                post("/api/patient-access-logs")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(Map.of("action", "VIEW", "patientId", patient.getId())))
            )
            .andExpect(status().isForbidden());
        assertThat(patientAccessLogRepository.countByPatientId(patient.getId())).isZero();
    }

    private int entriesOf(Patient patient, String token) throws Exception {
        return mockMvc
            .perform(get("/api/patient-access-logs?patientId=" + patient.getId()).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
            .andReturn()
            .getResponse()
            .getStatus();
    }

    private Patient patient(String purpose) {
        Patient patient = new Patient();
        patient.setHospitalId(hospitalIdService.nextPermanentId());
        patient.setFullName("Chart Opened " + purpose);
        patient.setSex(Sex.MALE);
        patient.setSexEstimated(false);
        patient.setRegistrationStatus(RegistrationStatus.COMPLETE);
        return patientRepository.saveAndFlush(patient);
    }

    private User account(String purpose, String authority) {
        String login = "chart-" + purpose + "-" + UUID.randomUUID().toString().substring(0, 8);
        User user = new User();
        user.setLogin(login);
        user.setPassword(passwordEncoder.encode(PASSWORD));
        user.setEmail(login + "@localhost");
        user.setActivated(true);
        user.setLangKey("en");
        user.getAuthorities().add(authorityRepository.findById(authority).orElseThrow());
        return userRepository.saveAndFlush(user);
    }

    private String signIn(User account) throws Exception {
        MvcResult result = mockMvc
            .perform(
                post("/api/authenticate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"username\":\"" + account.getLogin() + "\",\"password\":\"" + PASSWORD + "\",\"rememberMe\":false}")
            )
            .andReturn();
        assertThat(result.getResponse().getStatus()).as("sign-in refused: " + result.getResponse().getContentAsString()).isEqualTo(200);
        return om.readTree(result.getResponse().getContentAsString()).get("id_token").asText();
    }
}
