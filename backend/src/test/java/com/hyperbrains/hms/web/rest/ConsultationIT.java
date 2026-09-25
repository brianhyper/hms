package com.hyperbrains.hms.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hyperbrains.hms.IntegrationTest;
import com.hyperbrains.hms.domain.Bill;
import com.hyperbrains.hms.domain.BillLineItem;
import com.hyperbrains.hms.domain.Consultation;
import com.hyperbrains.hms.domain.Patient;
import com.hyperbrains.hms.domain.Visit;
import com.hyperbrains.hms.domain.enumeration.BillLineSourceType;
import com.hyperbrains.hms.domain.enumeration.ConsultationStatus;
import com.hyperbrains.hms.domain.enumeration.RegistrationStatus;
import com.hyperbrains.hms.domain.enumeration.Sex;
import com.hyperbrains.hms.domain.enumeration.VisitPriority;
import com.hyperbrains.hms.domain.enumeration.VisitStatus;
import com.hyperbrains.hms.domain.enumeration.VisitType;
import com.hyperbrains.hms.repository.BillLineItemRepository;
import com.hyperbrains.hms.repository.BillRepository;
import com.hyperbrains.hms.repository.ConsultationAddendumRepository;
import com.hyperbrains.hms.repository.ConsultationRepository;
import com.hyperbrains.hms.repository.HospitalServiceRepository;
import com.hyperbrains.hms.repository.PatientRepository;
import com.hyperbrains.hms.repository.VisitRepository;
import com.hyperbrains.hms.repository.VitalSignsRepository;
import com.hyperbrains.hms.service.BusinessRuleViolationException;
import com.hyperbrains.hms.service.HospitalIdService;
import com.hyperbrains.hms.service.dto.ConsultationDTO;
import com.hyperbrains.hms.service.dto.VisitDTO;
import com.hyperbrains.hms.service.dto.view.AddConsultationAddendumRequestDTO;
import com.hyperbrains.hms.service.dto.view.StartConsultationRequestDTO;
import com.hyperbrains.hms.service.dto.view.UpdateConsultationRequestDTO;
import com.hyperbrains.hms.service.dto.view.VisitIntakeRequestDTO;
import com.hyperbrains.hms.service.dto.view.VitalsSubmissionRequestDTO;
import com.hyperbrains.hms.service.workflow.ConsultationWorkflowService;
import com.hyperbrains.hms.service.workflow.TriageService;
import com.hyperbrains.hms.service.workflow.VisitIntakeService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

/**
 * Integration tests for the consultation and the two different correction rules.
 *
 * <p>The consultation fee is read from the catalogue rather than hard-coded, so changing the fee does
 * not mean chasing a number through the tests. They assert on the line item's <em>existence</em> and on
 * the bill total matching the catalogue price, never on a particular amount.
 */
@IntegrationTest
@WithMockUser(value = "admin", authorities = "ROLE_DOCTOR")
class ConsultationIT {

    @Autowired
    private ConsultationWorkflowService consultationService;

    @Autowired
    private VisitIntakeService visitIntakeService;

    @Autowired
    private TriageService triageService;

    @Autowired
    private VisitRepository visitRepository;

    @Autowired
    private ConsultationRepository consultationRepository;

    @Autowired
    private ConsultationAddendumRepository addendumRepository;

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private BillLineItemRepository billLineItemRepository;

    @Autowired
    private VitalSignsRepository vitalSignsRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private HospitalServiceRepository hospitalServiceRepository;

    @Autowired
    private HospitalIdService hospitalIdService;

    private Patient patient;

    @BeforeEach
    void setUp() {
        patient = new Patient();
        patient.setHospitalId(hospitalIdService.nextPermanentId());
        patient.setFullName("Consultation Test Patient");
        patient.setSex(Sex.FEMALE);
        patient.setSexEstimated(false);
        patient.setRegistrationStatus(RegistrationStatus.COMPLETE);
        patient = patientRepository.save(patient);
    }

    @AfterEach
    void cleanup() {
        List<Visit> visits = patient == null || patient.getId() == null
            ? List.of()
            : visitRepository.findByPatientId(patient.getId());

        List<Long> consultationIds = visits
            .stream()
            .map(Visit::getConsultation)
            .filter(Objects::nonNull)
            .map(Consultation::getId)
            .filter(Objects::nonNull)
            .toList();
        List<Long> billIds = visits.stream().map(Visit::getBill).filter(Objects::nonNull).map(Bill::getId).filter(Objects::nonNull).toList();
        List<Long> vitalSignsIds = visits
            .stream()
            .map(Visit::getVitals)
            .filter(Objects::nonNull)
            .map(vitals -> vitals.getId())
            .filter(Objects::nonNull)
            .toList();

        // Addenda reference the consultation; the visit references both the consultation and the
        // bill. Tearing down in the wrong order fails on a foreign key and reads like a product bug.
        consultationIds.forEach(id ->
            addendumRepository.findByConsultationIdOrderByCreatedAtAsc(id).forEach(addendumRepository::delete)
        );
        visits.forEach(visitRepository::delete);
        consultationIds.forEach(id -> consultationRepository.findById(id).ifPresent(consultationRepository::delete));
        billIds.forEach(id -> {
            billLineItemRepository.findByBillIdOrderByIdAsc(id).forEach(billLineItemRepository::delete);
            billRepository.findById(id).ifPresent(billRepository::delete);
        });
        vitalSignsIds.forEach(id -> vitalSignsRepository.findById(id).ifPresent(vitalSignsRepository::delete));
        if (patient != null && patient.getId() != null) {
            patientRepository.findById(patient.getId()).ifPresent(patientRepository::delete);
        }
    }

    @Test
    void startingAConsultationClaimsThePatientAndMovesTheVisit() {
        Visit visit = visitAwaitingDoctor();

        ConsultationDTO consultation = consultationService.start(visit.getId(), new StartConsultationRequestDTO());

        assertThat(consultation.getStatus()).isEqualTo(ConsultationStatus.IN_PROGRESS);
        assertThat(consultation.getStartedAt()).isNotNull();
        assertThat(visitRepository.findById(visit.getId()).orElseThrow().getStatus()).isEqualTo(VisitStatus.IN_CONSULTATION);
        assertThat(visitRepository.findById(visit.getId()).orElseThrow().getStartedConsultationAt()).isNotNull();
    }

    /** The doctor is taken from the authenticated principal, never from the request body. */
    @Test
    void theConsultationIsAttributedToTheAuthenticatedDoctor() {
        Visit visit = visitAwaitingDoctor();

        ConsultationDTO consultation = consultationService.start(visit.getId(), new StartConsultationRequestDTO());

        assertThat(consultation.getDoctor()).isNotNull();
        assertThat(consultation.getDoctor().getLogin()).isEqualTo("admin");
    }

    /** Re-opening the screen must not open a second consultation — that would bill the fee twice. */
    @Test
    void startingTwiceReturnsTheSameConsultation() {
        Visit visit = visitAwaitingDoctor();

        ConsultationDTO first = consultationService.start(visit.getId(), new StartConsultationRequestDTO());
        ConsultationDTO second = consultationService.start(visit.getId(), new StartConsultationRequestDTO());

        assertThat(second.getId()).isEqualTo(first.getId());
    }

    @Test
    void aPatientWhoHasNotBeenTriagedCannotBeSeenByADoctor() {
        VisitIntakeRequestDTO intake = new VisitIntakeRequestDTO();
        intake.setPatientId(patient.getId());
        intake.setType(VisitType.OUTPATIENT);
        intake.setPriority(VisitPriority.NORMAL);
        intake.setReasonForVisit("Not yet triaged");
        VisitDTO created = visitIntakeService.createVisit(intake);
        Visit visit = visitRepository.findById(created.getId()).orElseThrow();

        assertThatThrownBy(() -> consultationService.start(visit.getId(), new StartConsultationRequestDTO()))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("not waiting for a doctor");
    }

    @Test
    void completingAConsultationChargesTheFeeImmediately() {
        Visit visit = visitAwaitingDoctor();
        ConsultationDTO consultation = consultationService.start(visit.getId(), new StartConsultationRequestDTO());

        ConsultationDTO completed = consultationService.complete(consultation.getId(), notes("Sore throat", "Throat inflamed"));

        assertThat(completed.getStatus()).isEqualTo(ConsultationStatus.COMPLETED);
        assertThat(completed.getCompletedAt()).isNotNull();

        Visit reloaded = visitRepository.findById(visit.getId()).orElseThrow();
        assertThat(reloaded.getBill()).isNotNull();

        // Read the bill back through the repository: the visit's association is a lazy proxy and
        // this test runs outside a session, so touching anything but its id would fail to initialise.
        Long billId = reloaded.getBill().getId();
        Bill bill = billRepository.findById(billId).orElseThrow();

        // The charge exists before the visit is anywhere near the payment desk.
        List<BillLineItem> lines = billLineItemRepository.findByBillIdOrderByIdAsc(billId);
        assertThat(lines).singleElement().satisfies(line -> {
            assertThat(line.getSourceType()).isEqualTo(BillLineSourceType.CONSULTATION);
            assertThat(line.getSourceRef()).isEqualTo("CONSULTATION:" + consultation.getId());
        });

        BigDecimal cataloguePrice = hospitalServiceRepository.findOneByCode("CONSULTATION").orElseThrow().getPrice();
        assertThat(bill.getTotalAmount()).isEqualByComparingTo(cataloguePrice);
    }

    @Test
    void completingWithNothingOutstandingSendsTheVisitToPayment() {
        Visit visit = visitAwaitingDoctor();
        ConsultationDTO consultation = consultationService.start(visit.getId(), new StartConsultationRequestDTO());

        consultationService.complete(consultation.getId(), notes("Headache", null));

        assertThat(visitRepository.findById(visit.getId()).orElseThrow().getStatus()).isEqualTo(VisitStatus.WAITING_PAYMENT);
    }

    @Test
    void aConsultationCannotBeCompletedTwice() {
        Visit visit = visitAwaitingDoctor();
        ConsultationDTO consultation = consultationService.start(visit.getId(), new StartConsultationRequestDTO());
        consultationService.complete(consultation.getId(), notes("Done", null));

        assertThatThrownBy(() -> consultationService.complete(consultation.getId(), notes("Done again", null)))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("already completed");

        // And the retry cannot have charged a second time.
        Visit reloaded = visitRepository.findById(visit.getId()).orElseThrow();
        assertThat(billLineItemRepository.findByBillIdOrderByIdAsc(reloaded.getBill().getId())).hasSize(1);
    }

    /** Corrections to a Patient or VitalSigns are edited in place; a consultation in progress is the same. */
    @Test
    void notesAreEditableWhileTheConsultationIsInProgress() {
        Visit visit = visitAwaitingDoctor();
        ConsultationDTO consultation = consultationService.start(visit.getId(), new StartConsultationRequestDTO());

        consultationService.updateNotes(consultation.getId(), notes("First version", "Some findings"));
        ConsultationDTO updated = consultationService.updateNotes(consultation.getId(), notes("Corrected version", "Some findings"));

        assertThat(updated.getPresentingComplaint()).isEqualTo("Corrected version");
    }

    /**
     * The rule that is the opposite of the one above: once the consultation is complete its notes
     * must not be overwritten at all.
     */
    @Test
    void notesCannotBeChangedOnceTheConsultationIsComplete() {
        Visit visit = visitAwaitingDoctor();
        ConsultationDTO consultation = consultationService.start(visit.getId(), new StartConsultationRequestDTO());
        consultationService.complete(consultation.getId(), notes("Original findings", null));

        assertThatThrownBy(() -> consultationService.updateNotes(consultation.getId(), notes("Rewritten history", null)))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("add an addendum");

        // And the original really is untouched.
        assertThat(consultationRepository.findById(consultation.getId()).orElseThrow().getPresentingComplaint()).isEqualTo(
            "Original findings"
        );
    }

    @Test
    void anAddendumAppendsToACompletedConsultationWithoutTouchingIt() {
        Visit visit = visitAwaitingDoctor();
        ConsultationDTO consultation = consultationService.start(visit.getId(), new StartConsultationRequestDTO());
        consultationService.complete(consultation.getId(), notes("Original findings", null));

        AddConsultationAddendumRequestDTO request = new AddConsultationAddendumRequestDTO();
        request.setBody("Lab called back: culture positive, recall the patient");

        consultationService.addAddendum(consultation.getId(), request);

        assertThat(consultationService.listAddenda(consultation.getId()))
            .singleElement()
            .satisfies(addendum -> {
                assertThat(addendum.getBody()).contains("culture positive");
                assertThat(addendum.getAuthorLogin()).isEqualTo("admin");
            });
        assertThat(consultationRepository.findById(consultation.getId()).orElseThrow().getPresentingComplaint()).isEqualTo(
            "Original findings"
        );
    }

    /** An addendum exists because the original can no longer be edited, so it has no purpose earlier. */
    @Test
    void anAddendumCannotBeAddedWhileTheConsultationIsStillInProgress() {
        Visit visit = visitAwaitingDoctor();
        ConsultationDTO consultation = consultationService.start(visit.getId(), new StartConsultationRequestDTO());

        AddConsultationAddendumRequestDTO request = new AddConsultationAddendumRequestDTO();
        request.setBody("Too early");

        assertThatThrownBy(() -> consultationService.addAddendum(consultation.getId(), request))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("completed consultation");
    }

    private Visit visitAwaitingDoctor() {
        VisitIntakeRequestDTO intake = new VisitIntakeRequestDTO();
        intake.setPatientId(patient.getId());
        intake.setType(VisitType.OUTPATIENT);
        intake.setPriority(VisitPriority.NORMAL);
        intake.setReasonForVisit("Consultation test");
        VisitDTO created = visitIntakeService.createVisit(intake);

        VitalsSubmissionRequestDTO vitals = new VitalsSubmissionRequestDTO();
        vitals.setTemperature(BigDecimal.valueOf(36.8));
        vitals.setPulseRate(72);
        vitals.setSystolicBp(118);
        vitals.setDiastolicBp(76);
        vitals.setOxygenSaturation(98);
        triageService.submitVitals(created.getId(), vitals);

        // Return the managed entity so tests can assert on it without extra round trips.
        return visitRepository.findById(created.getId()).orElseThrow();
    }

    private static UpdateConsultationRequestDTO notes(String complaint, String findings) {
        UpdateConsultationRequestDTO request = new UpdateConsultationRequestDTO();
        request.setPresentingComplaint(complaint);
        request.setExaminationFindings(findings);
        return request;
    }
}
