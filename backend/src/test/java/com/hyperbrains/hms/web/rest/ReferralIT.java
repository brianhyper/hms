package com.hyperbrains.hms.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hyperbrains.hms.IntegrationTest;
import com.hyperbrains.hms.domain.Bill;
import com.hyperbrains.hms.domain.Consultation;
import com.hyperbrains.hms.domain.LabTest;
import com.hyperbrains.hms.domain.Patient;
import com.hyperbrains.hms.domain.Referral;
import com.hyperbrains.hms.domain.Visit;
import com.hyperbrains.hms.domain.enumeration.OrderType;
import com.hyperbrains.hms.domain.enumeration.ReferralStatus;
import com.hyperbrains.hms.domain.enumeration.ReferralType;
import com.hyperbrains.hms.domain.enumeration.RegistrationStatus;
import com.hyperbrains.hms.domain.enumeration.Sex;
import com.hyperbrains.hms.domain.enumeration.VisitPriority;
import com.hyperbrains.hms.domain.enumeration.VisitStatus;
import com.hyperbrains.hms.domain.enumeration.VisitType;
import com.hyperbrains.hms.repository.BillLineItemRepository;
import com.hyperbrains.hms.repository.BillRepository;
import com.hyperbrains.hms.repository.ConsultationRepository;
import com.hyperbrains.hms.repository.DiagnosticOrderRepository;
import com.hyperbrains.hms.repository.LabTestRepository;
import com.hyperbrains.hms.repository.PatientRepository;
import com.hyperbrains.hms.repository.ReferralRepository;
import com.hyperbrains.hms.repository.ResultRepository;
import com.hyperbrains.hms.repository.VisitRepository;
import com.hyperbrains.hms.repository.VitalSignsRepository;
import com.hyperbrains.hms.service.BusinessRuleViolationException;
import com.hyperbrains.hms.service.HospitalIdService;
import com.hyperbrains.hms.service.dto.VisitDTO;
import com.hyperbrains.hms.service.dto.view.CreateReferralRequestDTO;
import com.hyperbrains.hms.service.dto.view.EnterResultRequestDTO;
import com.hyperbrains.hms.service.dto.view.PlaceDiagnosticOrderRequestDTO;
import com.hyperbrains.hms.service.dto.view.ReferralViewDTO;
import com.hyperbrains.hms.service.dto.view.StartConsultationRequestDTO;
import com.hyperbrains.hms.service.dto.view.UpdateConsultationRequestDTO;
import com.hyperbrains.hms.service.dto.view.VisitIntakeRequestDTO;
import com.hyperbrains.hms.service.dto.view.VitalsSubmissionRequestDTO;
import com.hyperbrains.hms.service.workflow.ConsultationWorkflowService;
import com.hyperbrains.hms.service.workflow.DiagnosticOrderWorkflowService;
import com.hyperbrains.hms.service.workflow.ReferralWorkflowService;
import com.hyperbrains.hms.service.workflow.TriageService;
import com.hyperbrains.hms.service.workflow.VisitIntakeService;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Integration tests for referring a patient out.
 *
 * <p>Two behaviours are being held here, and the specification insists they stay separate. Ending the
 * local journey moves the visit toward payment <em>as though nothing were outstanding</em>, so tests
 * this hospital will never finish do not hold the patient at the desk. Producing and sending the letter
 * is a document workflow that can fail on its own without undoing any of that.
 *
 * <p>Mail is mocked, so the attachment can be captured and parsed rather than trusted.
 */
@IntegrationTest
@WithMockUser(value = "admin", authorities = "ROLE_DOCTOR")
class ReferralIT {

    private static final BigDecimal LAB_PRICE = new BigDecimal("25.00");

    @Autowired
    private ReferralWorkflowService referralService;

    @Autowired
    private DiagnosticOrderWorkflowService orderService;

    @Autowired
    private VisitIntakeService visitIntakeService;

    @Autowired
    private TriageService triageService;

    @Autowired
    private ConsultationWorkflowService consultationService;

    @Autowired
    private VisitRepository visitRepository;

    @Autowired
    private ConsultationRepository consultationRepository;

    @Autowired
    private ReferralRepository referralRepository;

    @Autowired
    private DiagnosticOrderRepository diagnosticOrderRepository;

    @Autowired
    private ResultRepository resultRepository;

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private BillLineItemRepository billLineItemRepository;

    @Autowired
    private VitalSignsRepository vitalSignsRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private LabTestRepository labTestRepository;

    @Autowired
    private HospitalIdService hospitalIdService;

    @MockitoBean
    private JavaMailSender javaMailSender;

    private Patient patient;

    private LabTest labTest;

    @BeforeEach
    void setUp() {
        // MailService asks the sender for a message; without this stub it receives null.
        when(javaMailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
        doNothing().when(javaMailSender).send(any(MimeMessage.class));

        patient = new Patient();
        patient.setHospitalId(hospitalIdService.nextPermanentId());
        patient.setFullName("Referral Test Patient");
        patient.setSex(Sex.FEMALE);
        patient.setSexEstimated(false);
        patient.setRegistrationStatus(RegistrationStatus.COMPLETE);
        patient = patientRepository.save(patient);

        labTest = new LabTest();
        labTest.setName("Full Blood Count");
        labTest.setPrice(LAB_PRICE);
        labTest.setActive(true);
        labTest = labTestRepository.save(labTest);
    }

    @AfterEach
    void cleanup() {
        List<Visit> visits = patient == null || patient.getId() == null
            ? List.of()
            : visitRepository.findByPatientId(patient.getId());

        List<Long> referralIds = visits
            .stream()
            .flatMap(visit -> referralRepository.findByVisitIdOrderByCreatedAt(visit.getId()).stream())
            .map(Referral::getId)
            .toList();
        List<Long> orderIds = visits
            .stream()
            .flatMap(visit -> diagnosticOrderRepository.findByVisitIdOrderByOrderedAtAsc(visit.getId()).stream())
            .map(order -> order.getId())
            .toList();
        List<Long> resultIds = orderIds
            .stream()
            .flatMap(id -> diagnosticOrderRepository.findById(id).stream())
            .map(order -> order.getResult())
            .filter(Objects::nonNull)
            .map(result -> result.getId())
            .filter(Objects::nonNull)
            .toList();
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
            .map(visit -> visit.getVitals())
            .filter(Objects::nonNull)
            .map(vitals -> vitals.getId())
            .filter(Objects::nonNull)
            .toList();

        // A referral points at its visit and an order at both its visit and its result, so children go
        // first or the teardown fails on a foreign key and reads like a product bug.
        referralIds.forEach(id -> referralRepository.findById(id).ifPresent(referralRepository::delete));
        orderIds.forEach(id -> diagnosticOrderRepository.findById(id).ifPresent(diagnosticOrderRepository::delete));
        resultIds.forEach(id -> resultRepository.findById(id).ifPresent(resultRepository::delete));
        visits.forEach(visit -> visitRepository.deleteById(visit.getId()));
        consultationIds.forEach(id -> consultationRepository.findById(id).ifPresent(consultationRepository::delete));
        billIds.forEach(id -> {
            billLineItemRepository.findByBillIdOrderByIdAsc(id).forEach(billLineItemRepository::delete);
            billRepository.findById(id).ifPresent(billRepository::delete);
        });
        vitalSignsIds.forEach(id -> vitalSignsRepository.findById(id).ifPresent(vitalSignsRepository::delete));
        if (patient != null && patient.getId() != null) {
            patientRepository.findById(patient.getId()).ifPresent(patientRepository::delete);
        }
        if (labTest != null && labTest.getId() != null) {
            labTestRepository.findById(labTest.getId()).ifPresent(labTestRepository::delete);
        }
    }

    // ---------------------------------------------------------------- ending the local journey

    /**
     * The rule the specification spells out: a referral is treated the same way as "nothing further is
     * pending", rather than leaving the patient waiting on tests this hospital will never finish.
     */
    @Test
    void aReferralEndsTheLocalJourneyEvenWithATestStillOutstanding() {
        Visit visit = visitWithOutstandingOrder();
        assertThat(statusOf(visit)).isEqualTo(VisitStatus.WAITING_RESULTS);

        referralService.create(visit.getId(), referral("Kenyatta National Hospital"));

        assertThat(statusOf(visit)).isEqualTo(VisitStatus.WAITING_PAYMENT);
    }

    /**
     * Once referred, a result arriving later is still charged for — the hospital did the work — but it
     * must not drag the encounter back out of the payment queue, where it would sit waiting for a
     * sequence nobody is running any more.
     */
    @Test
    void aLateResultDoesNotDragAReferredVisitBackwards() {
        Visit visit = visitWithOutstandingOrder();
        Long orderId = orderIdOf(visit);
        referralService.create(visit.getId(), referral("Kenyatta National Hospital"));
        BigDecimal beforeResult = totalOf(visit);

        orderService.enterResult(orderId, result("Normal"));

        assertThat(statusOf(visit)).isEqualTo(VisitStatus.WAITING_PAYMENT);
        // The test still has to be paid for, so the total has to move.
        assertThat(totalOf(visit)).isEqualByComparingTo(beforeResult.add(LAB_PRICE));
    }

    /**
     * Referring mid-consultation would move the visit to payment without the fee ever being raised or
     * the notes finished, so it is refused with instructions rather than a bare rejection.
     */
    @Test
    void aReferralCannotBeWrittenWhileTheConsultationIsStillOpen() {
        Visit visit = visitAwaitingDoctor();
        consultationService.start(visit.getId(), new StartConsultationRequestDTO());

        assertThatThrownBy(() -> referralService.create(visit.getId(), referral("Kenyatta National Hospital")))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("Complete the consultation");
    }

    // ---------------------------------------------------------------- the letter

    @Test
    void theLetterIsARealPdfCarryingThePatientAndTheDestination() throws Exception {
        Visit visit = visitAwaitingDoctor();
        startAndCompleteConsultation(visit);
        ReferralViewDTO referral = referralService.create(visit.getId(), referral("Kenyatta National Hospital"));

        ReferralWorkflowService.RenderedDocument document = referralService.renderLetter(referral.referralId());

        assertThat(document.contentType()).isEqualTo("application/pdf");
        assertThat(document.filename()).startsWith("referral-").endsWith(".pdf");
        assertThat(new String(document.content(), 0, 5)).isEqualTo("%PDF-");
        try (PDDocument pdf = Loader.loadPDF(document.content())) {
            String text = new PDFTextStripper().getText(pdf);
            assertThat(text).contains(patient.getFullName());
            assertThat(text).contains(patient.getHospitalId());
            assertThat(text).contains("Kenyatta National Hospital");
            assertThat(text).contains("Suspected pulmonary tuberculosis");
        }
    }

    /** Generating a letter is not sending it; until it has left, somebody still has to chase it. */
    @Test
    void generatingTheLetterDoesNotMarkTheReferralDispatched() {
        Visit visit = visitAwaitingDoctor();
        startAndCompleteConsultation(visit);
        ReferralViewDTO referral = referralService.create(visit.getId(), referral("Kenyatta National Hospital"));

        referralService.renderLetter(referral.referralId());

        assertThat(referralRepository.findById(referral.referralId()).orElseThrow().getStatus()).isEqualTo(ReferralStatus.PENDING);
    }

    @Test
    void emailingTheLetterSendsARealPdfAndMarksTheReferralComplete() throws Exception {
        Visit visit = visitAwaitingDoctor();
        startAndCompleteConsultation(visit);
        ReferralViewDTO referral = referralService.create(visit.getId(), referral("Kenyatta National Hospital"));

        ReferralViewDTO dispatched = referralService.emailLetter(referral.referralId());

        assertThat(dispatched.status()).isEqualTo(ReferralStatus.COMPLETED);

        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(javaMailSender).send(captor.capture());
        MimeMessage message = captor.getValue();
        assertThat(message.getAllRecipients()[0]).hasToString("referrals@knh.example");
        assertThat(message.getSubject()).contains(patient.getFullName());

        byte[] attached = attachedPdf(message);
        assertThat(new String(attached, 0, 5)).isEqualTo("%PDF-");
        try (PDDocument pdf = Loader.loadPDF(attached)) {
            assertThat(new PDFTextStripper().getText(pdf)).contains(patient.getFullName());
        }
    }

    @Test
    void aReferralWithNoDestinationAddressCannotBeEmailed() {
        Visit visit = visitAwaitingDoctor();
        startAndCompleteConsultation(visit);
        CreateReferralRequestDTO request = referral("Kenyatta National Hospital");
        request.setDestinationEmail(null);
        ReferralViewDTO referral = referralService.create(visit.getId(), request);

        assertThatThrownBy(() -> referralService.emailLetter(referral.referralId()))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("no destination email address");
    }

    /**
     * The failure this guards against is silent: a referral marked dispatched that never arrived means a
     * patient turned away at the receiving facility, with nobody here knowing to chase it.
     */
    @Test
    void aFailedSendLeavesTheReferralPending() {
        Visit visit = visitAwaitingDoctor();
        startAndCompleteConsultation(visit);
        ReferralViewDTO referral = referralService.create(visit.getId(), referral("Kenyatta National Hospital"));
        doThrow(new MailSendException("SMTP is down")).when(javaMailSender).send(any(MimeMessage.class));

        assertThatThrownBy(() -> referralService.emailLetter(referral.referralId())).isInstanceOf(IllegalStateException.class);

        assertThat(referralRepository.findById(referral.referralId()).orElseThrow().getStatus()).isEqualTo(ReferralStatus.PENDING);
    }

    @Test
    void everythingWrittenForTheVisitIsListedOldestFirst() {
        Visit visit = visitAwaitingDoctor();
        startAndCompleteConsultation(visit);
        referralService.create(visit.getId(), referral("First Destination"));
        referralService.create(visit.getId(), referral("Second Destination"));

        List<String> destinations = referralService
            .forVisit(visit.getId())
            .stream()
            .map(ReferralViewDTO::destination)
            .toList();

        assertThat(destinations).containsExactly("First Destination", "Second Destination");
    }

    // ---------------------------------------------------------------- helpers

    private Visit visitAwaitingDoctor() {
        VisitIntakeRequestDTO intake = new VisitIntakeRequestDTO();
        intake.setPatientId(patient.getId());
        intake.setType(VisitType.OUTPATIENT);
        intake.setPriority(VisitPriority.NORMAL);
        intake.setReasonForVisit("Referral test");
        VisitDTO created = visitIntakeService.createVisit(intake);

        VitalsSubmissionRequestDTO vitals = new VitalsSubmissionRequestDTO();
        vitals.setTemperature(BigDecimal.valueOf(37.4));
        vitals.setPulseRate(84);
        vitals.setSystolicBp(121);
        vitals.setDiastolicBp(79);
        vitals.setOxygenSaturation(96);
        triageService.submitVitals(created.getId(), vitals);

        return visitRepository.findById(created.getId()).orElseThrow();
    }

    private void startAndCompleteConsultation(Visit visit) {
        Long consultationId = consultationService.start(visit.getId(), new StartConsultationRequestDTO()).getId();
        UpdateConsultationRequestDTO notes = new UpdateConsultationRequestDTO();
        notes.setPresentingComplaint("Chronic cough");
        consultationService.complete(consultationId, notes);
    }

    /** A visit whose consultation is finished but whose lab test has not come back. */
    private Visit visitWithOutstandingOrder() {
        Visit visit = visitAwaitingDoctor();
        Long consultationId = consultationService.start(visit.getId(), new StartConsultationRequestDTO()).getId();

        PlaceDiagnosticOrderRequestDTO order = new PlaceDiagnosticOrderRequestDTO();
        order.setType(OrderType.LAB);
        order.setLabTestId(labTest.getId());
        orderService.place(visit.getId(), order);

        UpdateConsultationRequestDTO notes = new UpdateConsultationRequestDTO();
        notes.setPresentingComplaint("Suspected tuberculosis");
        consultationService.complete(consultationId, notes);

        return visitRepository.findById(visit.getId()).orElseThrow();
    }

    private static CreateReferralRequestDTO referral(String destination) {
        CreateReferralRequestDTO request = new CreateReferralRequestDTO();
        request.setType(ReferralType.EXTERNAL);
        request.setDestination(destination);
        request.setDestinationEmail("referrals@knh.example");
        request.setReason("Suspected pulmonary tuberculosis");
        return request;
    }

    private static EnterResultRequestDTO result(String value) {
        EnterResultRequestDTO request = new EnterResultRequestDTO();
        request.setResultValue(value);
        return request;
    }

    private Long orderIdOf(Visit visit) {
        return diagnosticOrderRepository.findByVisitIdOrderByOrderedAtAsc(visit.getId()).getFirst().getId();
    }

    private VisitStatus statusOf(Visit visit) {
        return visitRepository.findById(visit.getId()).orElseThrow().getStatus();
    }

    private BigDecimal totalOf(Visit visit) {
        // The visit's association is lazy and these tests run outside a session, so the bill is read back
        // by id rather than touched through the visit.
        Bill bill = billRepository.findById(visitRepository.findById(visit.getId()).orElseThrow().getBill().getId()).orElseThrow();
        return bill.getTotalAmount();
    }

    /**
     * Pulls the PDF out of the captured message.
     *
     * <p>The message is serialised and re-parsed first, which is the only honest way to inspect it: a
     * {@code MimeBodyPart} reports {@code text/plain} for its content type until the headers are
     * materialised, and a stubbed send never materialises them. Unwritten parts would therefore look
     * wrong however they were actually built, and the serialised form is what a mail server receives.
     */
    private static byte[] attachedPdf(MimeMessage message) throws Exception {
        ByteArrayOutputStream raw = new ByteArrayOutputStream();
        message.writeTo(raw);
        MimeMessage received = new MimeMessage((Session) null, new ByteArrayInputStream(raw.toByteArray()));

        List<byte[]> found = new ArrayList<>();
        List<String> seen = new ArrayList<>();
        collectAttachments(received.getContent(), found, seen);
        assertThat(found).as("PDF attachments; MIME parts seen: %s", seen).hasSize(1);
        return found.getFirst();
    }

    private static void collectAttachments(Object part, List<byte[]> found, List<String> seen) throws Exception {
        if (part instanceof Multipart multipart) {
            for (int i = 0; i < multipart.getCount(); i++) {
                collectAttachments(multipart.getBodyPart(i), found, seen);
            }
        } else if (part instanceof MimeBodyPart bodyPart) {
            seen.add(bodyPart.getContentType());
            if (bodyPart.getContentType().toLowerCase().startsWith("application/pdf")) {
                found.add(bodyPart.getInputStream().readAllBytes());
            }
        }
    }
}
