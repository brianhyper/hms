package com.hyperbrains.hms.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hyperbrains.hms.IntegrationTest;
import com.hyperbrains.hms.domain.Admission;
import com.hyperbrains.hms.domain.Bill;
import com.hyperbrains.hms.domain.Bed;
import com.hyperbrains.hms.domain.DoctorOrder;
import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.domain.Visit;
import com.hyperbrains.hms.domain.enumeration.AdmissionStatus;
import com.hyperbrains.hms.domain.enumeration.BedStatus;
import com.hyperbrains.hms.domain.enumeration.BillStatus;
import com.hyperbrains.hms.domain.enumeration.DoctorOrderRecurrence;
import com.hyperbrains.hms.domain.enumeration.DoctorOrderStatus;
import com.hyperbrains.hms.domain.enumeration.DoctorOrderType;
import com.hyperbrains.hms.domain.enumeration.VisitStatus;
import com.hyperbrains.hms.repository.AdmissionRepository;
import com.hyperbrains.hms.repository.AuditLogRepository;
import com.hyperbrains.hms.repository.BedRepository;
import com.hyperbrains.hms.repository.BillRepository;
import com.hyperbrains.hms.repository.DoctorOrderRepository;
import com.hyperbrains.hms.repository.UserRepository;
import com.hyperbrains.hms.repository.VisitRepository;
import com.hyperbrains.hms.security.AuthoritiesConstants;
import com.hyperbrains.hms.service.AuditActions;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ending a stay, in two signatures.
 *
 * <p>What is asserted here is the shape of the rule rather than that an endpoint answers: one signature leaves the
 * patient in the bed, the same person cannot be both signatures, an unsettled bill stops it, an order that is still
 * running has to be seen, and only the half that completes the pair ends the stay and frees the bed. The caller is
 * the seeded {@code admin} account throughout, which is what makes the "one person cannot sign twice" case real
 * rather than a fixture that never reaches the check.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser(value = "admin", authorities = AuthoritiesConstants.DOCTOR)
class InpatientDischargeIT {

    private static final String DOCTOR_SIGN_OFF = "/api/admissions/{admissionId}/discharge/doctor";

    private static final String NURSE_SIGN_OFF = "/api/admissions/{admissionId}/discharge/nurse";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManager em;

    @Autowired
    private AdmissionRepository admissionRepository;

    @Autowired
    private BedRepository bedRepository;

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private DoctorOrderRepository doctorOrderRepository;

    @Autowired
    private VisitRepository visitRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    private Admission admission;
    private Visit visit;
    private Bed bed;
    private User doctor;

    @BeforeEach
    void setUp() {
        doctor = aUser("doctor");

        // A stay in a bed on the ward, which is the shape a discharge acts on: the visit is an inpatient encounter,
        // the admission holds a bed, and nothing has been signed.
        admission = AdmissionResourceIT.createEntity(em);
        visit = admission.getVisit();
        visit.setStatus(VisitStatus.ADMITTED);
        admission.setStatus(AdmissionStatus.ADMITTED);
        admission.setDischargedAt(null);
        admission.setDischargeNote(null);
        admission.setDischargedByDoctor(null);
        admission.setDischargedByNurse(null);

        bed = BedResourceIT.createEntity(em);
        bed.setStatus(BedStatus.OCCUPIED);
        em.persist(bed);
        admission.setBed(bed);

        em.persist(admission);
        em.flush();
    }

    /**
     * The billing gate, and the reason it exists: a patient who walks out on an unsettled bill is the case §7 names.
     */
    @Test
    @Transactional
    void anUnsettledBillStopsADischarge() throws Exception {
        anUnpaidBillOnTheVisit();

        mockMvc
            .perform(post(DOCTOR_SIGN_OFF, admission.getId()).contentType(MediaType.APPLICATION_JSON).content(aSignOff(false, null)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value("error.billUnsettledAtDischarge"));

        Admission stored = admissionRepository.findById(admission.getId()).orElseThrow();
        assertThat(stored.getStatus()).as("nothing was signed and nothing moved").isEqualTo(AdmissionStatus.ADMITTED);
        assertThat(stored.getDischargedByDoctor()).isNull();
    }

    /**
     * One signature is not a discharge. The stay carries on, the patient stays in the bed and the encounter stays
     * open — which is what having two signatures means.
     */
    @Test
    @Transactional
    void theDoctorsSignatureAloneDoesNotEndTheStay() throws Exception {
        mockMvc
            .perform(post(DOCTOR_SIGN_OFF, admission.getId()).contentType(MediaType.APPLICATION_JSON).content(aSignOff(false, "Going home")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.complete").value(false))
            .andExpect(jsonPath("$.status").value(AdmissionStatus.ADMITTED.toString()));

        Admission stored = admissionRepository.findById(admission.getId()).orElseThrow();
        assertThat(stored.getDischargedByDoctor().getLogin()).as("the signature is recorded").isEqualTo("admin");
        assertThat(stored.getStatus()).isEqualTo(AdmissionStatus.ADMITTED);
        assertThat(bedRepository.findById(bed.getId()).orElseThrow().getStatus()).isEqualTo(BedStatus.OCCUPIED);
        assertThat(visitRepository.findById(visit.getId()).orElseThrow().getStatus()).isEqualTo(VisitStatus.ADMITTED);
    }

    /**
     * §7's own words: letting one person give both signatures is a decision to record, not an accident to allow. It is
     * refused, and the fixture makes the refusal reachable — the nurse's half is already the caller's own account.
     */
    @Test
    @Transactional
    void theSamePersonCannotBeBothSignatures() throws Exception {
        admission.setDischargedByNurse(userRepository.findOneByLogin("admin").orElseThrow());
        admissionRepository.saveAndFlush(admission);

        mockMvc
            .perform(post(DOCTOR_SIGN_OFF, admission.getId()).contentType(MediaType.APPLICATION_JSON).content(aSignOff(false, null)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value("error.dischargeCannotBeSignedByOnePerson"));

        assertThat(admissionRepository.findById(admission.getId()).orElseThrow().getDischargedByDoctor()).isNull();
    }

    /**
     * The completing half: the stay ends, the bed goes to {@code CLEANING} rather than back to {@code AVAILABLE}, the
     * visit closes through the discharge path, and the note the last signer left is the discharge note.
     */
    @Test
    @Transactional
    void theSecondSignatureEndsTheStayAndFreesTheBed() throws Exception {
        admission.setDischargedByNurse(aUser("nurse"));
        admission.setDischargeNote("Nurse's handover note");
        admissionRepository.saveAndFlush(admission);

        mockMvc
            .perform(
                post(DOCTOR_SIGN_OFF, admission.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(aSignOff(false, "Doctor's discharge summary"))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.complete").value(true))
            .andExpect(jsonPath("$.status").value(AdmissionStatus.DISCHARGED.toString()));

        Admission stored = admissionRepository.findById(admission.getId()).orElseThrow();
        assertThat(stored.getDischargedAt()).as("a discharge has a moment").isNotNull();
        assertThat(stored.getDischargeNote()).isEqualTo("Doctor's discharge summary");
        assertThat(bedRepository.findById(bed.getId()).orElseThrow().getStatus())
            .as("a bed a patient has just left is not ready for the next one")
            .isEqualTo(BedStatus.CLEANING);
        assertThat(visitRepository.findById(visit.getId()).orElseThrow().getStatus()).isEqualTo(VisitStatus.CLOSED);
    }

    /** A sign-off with nothing to say leaves the note as it stands, so the first signer's text is not erased. */
    @Test
    @Transactional
    void aSilentSecondSignerDoesNotEraseTheNote() throws Exception {
        admission.setDischargedByNurse(aUser("nurse"));
        admission.setDischargeNote("Nurse's handover note");
        admissionRepository.saveAndFlush(admission);

        mockMvc
            .perform(post(DOCTOR_SIGN_OFF, admission.getId()).contentType(MediaType.APPLICATION_JSON).content(aSignOff(false, "   ")))
            .andExpect(status().isOk());

        assertThat(admissionRepository.findById(admission.getId()).orElseThrow().getDischargeNote())
            .isEqualTo("Nurse's handover note");
    }

    /**
     * An order that is still running is surfaced rather than silently discharged past, and proceeding anyway is a
     * decision the signer makes explicitly — and one that is logged, which is what §7 asks for.
     */
    @Test
    @Transactional
    void anOrderStillRunningIsSurfacedAndHasToBeAcknowledged() throws Exception {
        DoctorOrder running = aRunningOrder();

        mockMvc
            .perform(post(DOCTOR_SIGN_OFF, admission.getId()).contentType(MediaType.APPLICATION_JSON).content(aSignOff(false, null)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value("error.ordersStillRunningAtDischarge"));

        assertThat(admissionRepository.findById(admission.getId()).orElseThrow().getDischargedByDoctor())
            .as("a refused discharge records nothing")
            .isNull();

        admission.setDischargedByNurse(aUser("nurse"));
        admissionRepository.saveAndFlush(admission);

        mockMvc
            .perform(post(DOCTOR_SIGN_OFF, admission.getId()).contentType(MediaType.APPLICATION_JSON).content(aSignOff(true, null)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.complete").value(true))
            .andExpect(jsonPath("$.outstandingOrderIds[0]").value(running.getId().intValue()));

        assertThat(
            auditLogRepository
                .findByEntityNameAndEntityIdOrderByIdAsc("Admission", admission.getId().toString())
                .stream()
                .anyMatch(entry -> AuditActions.DISCHARGE_OUTSTANDING_ORDERS_ACKNOWLEDGED.equals(entry.getAction()))
        )
            .as("proceeding with orders still running is logged, which is the whole of the control")
            .isTrue();
    }

    @Test
    @Transactional
    void aStayThatHasAlreadyEndedCannotBeDischargedAgain() throws Exception {
        admission.setStatus(AdmissionStatus.DISCHARGED);
        admissionRepository.saveAndFlush(admission);

        mockMvc
            .perform(post(DOCTOR_SIGN_OFF, admission.getId()).contentType(MediaType.APPLICATION_JSON).content(aSignOff(false, null)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value("error.stayAlreadyEnded"));
    }

    /**
     * The two halves are different jobs done by different people, which is why they are two routes: a nurse cannot
     * take the doctor's half.
     */
    @Test
    @Transactional
    @WithMockUser(value = "admin", authorities = AuthoritiesConstants.NURSE)
    void aNurseCannotTakeTheDoctorsHalf() throws Exception {
        mockMvc
            .perform(post(DOCTOR_SIGN_OFF, admission.getId()).contentType(MediaType.APPLICATION_JSON).content(aSignOff(false, null)))
            .andExpect(status().isForbidden());

        mockMvc
            .perform(post(NURSE_SIGN_OFF, admission.getId()).contentType(MediaType.APPLICATION_JSON).content(aSignOff(false, null)))
            .andExpect(status().isOk());
    }

    @Test
    @Transactional
    @WithMockUser(value = "admin", authorities = AuthoritiesConstants.PHARMACY)
    void pharmacyEndsNobodyStay() throws Exception {
        mockMvc
            .perform(post(DOCTOR_SIGN_OFF, admission.getId()).contentType(MediaType.APPLICATION_JSON).content(aSignOff(false, null)))
            .andExpect(status().isForbidden());
        mockMvc
            .perform(post(NURSE_SIGN_OFF, admission.getId()).contentType(MediaType.APPLICATION_JSON).content(aSignOff(false, null)))
            .andExpect(status().isForbidden());
    }

    private Bill anUnpaidBillOnTheVisit() {
        Bill bill = billRepository.save(new Bill().totalAmount(new BigDecimal("500.00")).status(BillStatus.UNPAID));
        visit.setBill(bill);
        visitRepository.saveAndFlush(visit);
        return bill;
    }

    private DoctorOrder aRunningOrder() {
        return doctorOrderRepository.saveAndFlush(
            new DoctorOrder()
                .type(DoctorOrderType.INSTRUCTION)
                .recurrence(DoctorOrderRecurrence.RECURRING)
                .details("Hourly observations")
                .status(DoctorOrderStatus.ACTIVE)
                .orderedAt(Instant.now())
                .admission(admission)
                .orderedBy(doctor)
        );
    }

    /**
     * A real account for a fixture signature. The login is short and unique because {@code User.login} is capped at
     * fifty characters, which a full UUID suffix is not.
     */
    private User aUser(String prefix) {
        User user = UserResourceIT.createEntity();
        user.setLogin(prefix + "-" + UUID.randomUUID().toString().substring(0, 8));
        em.persist(user);
        em.flush();
        return user;
    }

    private static String aSignOff(boolean acknowledgeOutstandingOrders, String note) {
        return (
            "{\"acknowledgeOutstandingOrders\":" +
            acknowledgeOutstandingOrders +
            ",\"note\":" +
            (note == null ? "null" : "\"" + note + "\"") +
            "}"
        );
    }
}
