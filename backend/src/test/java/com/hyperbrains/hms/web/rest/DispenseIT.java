package com.hyperbrains.hms.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hyperbrains.hms.IntegrationTest;
import com.hyperbrains.hms.domain.Bill;
import com.hyperbrains.hms.domain.Consultation;
import com.hyperbrains.hms.domain.Dispense;
import com.hyperbrains.hms.domain.DispenseLine;
import com.hyperbrains.hms.domain.Drug;
import com.hyperbrains.hms.domain.Patient;
import com.hyperbrains.hms.domain.Prescription;
import com.hyperbrains.hms.domain.Visit;
import com.hyperbrains.hms.domain.enumeration.BillStatus;
import com.hyperbrains.hms.domain.enumeration.PaymentMethod;
import com.hyperbrains.hms.domain.enumeration.PrescriptionSource;
import com.hyperbrains.hms.domain.enumeration.PrescriptionStatus;
import com.hyperbrains.hms.domain.enumeration.RegistrationStatus;
import com.hyperbrains.hms.domain.enumeration.Sex;
import com.hyperbrains.hms.domain.enumeration.VisitPriority;
import com.hyperbrains.hms.domain.enumeration.VisitStatus;
import com.hyperbrains.hms.domain.enumeration.VisitType;
import com.hyperbrains.hms.repository.BillLineItemRepository;
import com.hyperbrains.hms.repository.BillRepository;
import com.hyperbrains.hms.repository.ConsultationRepository;
import com.hyperbrains.hms.repository.DispenseLineRepository;
import com.hyperbrains.hms.repository.DispenseRepository;
import com.hyperbrains.hms.repository.DrugRepository;
import com.hyperbrains.hms.repository.PatientRepository;
import com.hyperbrains.hms.repository.PrescriptionLineRepository;
import com.hyperbrains.hms.repository.PrescriptionRepository;
import com.hyperbrains.hms.repository.VisitRepository;
import com.hyperbrains.hms.repository.VitalSignsRepository;
import com.hyperbrains.hms.service.BusinessRuleViolationException;
import com.hyperbrains.hms.service.HospitalIdService;
import com.hyperbrains.hms.service.dto.VisitDTO;
import com.hyperbrains.hms.service.dto.view.DispenseLineRequestDTO;
import com.hyperbrains.hms.service.dto.view.DispenseRecordDTO;
import com.hyperbrains.hms.service.dto.view.DispenseRequestDTO;
import com.hyperbrains.hms.service.dto.view.PlacePrescriptionRequestDTO;
import com.hyperbrains.hms.service.dto.view.PrescriptionLineRequestDTO;
import com.hyperbrains.hms.service.dto.view.PrescriptionViewDTO;
import com.hyperbrains.hms.service.dto.view.RecordPaymentRequestDTO;
import com.hyperbrains.hms.service.dto.view.StartConsultationRequestDTO;
import com.hyperbrains.hms.service.dto.view.UpdateConsultationRequestDTO;
import com.hyperbrains.hms.service.dto.view.VisitIntakeRequestDTO;
import com.hyperbrains.hms.service.dto.view.VitalsSubmissionRequestDTO;
import com.hyperbrains.hms.service.workflow.ConsultationWorkflowService;
import com.hyperbrains.hms.service.workflow.DispenseWorkflowService;
import com.hyperbrains.hms.service.workflow.PaymentWorkflowService;
import com.hyperbrains.hms.service.workflow.PrescriptionWorkflowService;
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
 * Integration tests for handing medicine over, and for the payment gate that stands in front of it.
 *
 * <p>The gate is the thing under test as much as the hand-over is: medicine leaving the shelf before
 * the bill is settled is the failure that cannot be undone once the patient has walked out with it.
 */
@IntegrationTest
@WithMockUser(value = "admin", authorities = "ROLE_PHARMACY")
class DispenseIT {

    private static final BigDecimal DRUG_PRICE = new BigDecimal("2.50");

    @Autowired
    private DispenseWorkflowService dispenseService;

    @Autowired
    private PaymentWorkflowService paymentService;

    @Autowired
    private PrescriptionWorkflowService prescriptionService;

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
    private PrescriptionRepository prescriptionRepository;

    @Autowired
    private PrescriptionLineRepository prescriptionLineRepository;

    @Autowired
    private DispenseRepository dispenseRepository;

    @Autowired
    private DispenseLineRepository dispenseLineRepository;

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private BillLineItemRepository billLineItemRepository;

    @Autowired
    private VitalSignsRepository vitalSignsRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DrugRepository drugRepository;

    @Autowired
    private HospitalIdService hospitalIdService;

    private Patient patient;

    private Drug drug;

    @BeforeEach
    void setUp() {
        patient = new Patient();
        patient.setHospitalId(hospitalIdService.nextPermanentId());
        patient.setFullName("Dispense Test Patient");
        patient.setSex(Sex.FEMALE);
        patient.setSexEstimated(false);
        patient.setRegistrationStatus(RegistrationStatus.COMPLETE);
        patient = patientRepository.save(patient);

        drug = new Drug();
        drug.setName("Amoxicillin");
        drug.setUnit("capsule");
        drug.setPrice(DRUG_PRICE);
        drug.setCurrentStock(100);
        drug.setReservedStock(0);
        drug.setLowStockThreshold(10);
        drug.setActive(true);
        drug = drugRepository.save(drug);
    }

    @AfterEach
    void cleanup() {
        List<Visit> visits = patient == null || patient.getId() == null
            ? List.of()
            : visitRepository.findByPatientId(patient.getId());

        List<Prescription> prescriptions = visits
            .stream()
            .flatMap(visit -> prescriptionRepository.findByVisitId(visit.getId()).stream())
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

        // Deepest first: a dispense line points at both its dispense and a prescribed line, and a
        // dispense points at the prescription.
        prescriptions.forEach(prescription -> {
            List<DispenseLine> dispensedLines = dispenseLineRepository.findWithDrugByPrescriptionId(prescription.getId());
            List<Dispense> dispenses = dispensedLines.stream().map(DispenseLine::getDispense).distinct().toList();
            dispenseLineRepository.deleteAll(dispensedLines);
            dispenses.forEach(dispense -> dispenseRepository.deleteById(dispense.getId()));
            prescriptionLineRepository.deleteAll(prescriptionLineRepository.findWithDrugByPrescriptionId(prescription.getId()));
            prescriptionRepository.deleteById(prescription.getId());
        });
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
        if (drug != null && drug.getId() != null) {
            drugRepository.findById(drug.getId()).ifPresent(drugRepository::delete);
        }
    }

    /** The gate. A prescription whose bill is still owed is not the pharmacy's to hand over. */
    @Test
    void medicineCannotBeHandedOverBeforeTheBillIsPaid() {
        Prescribed prescribed = prescribe(5, false);
        assertThat(prescriptionRepository.findById(prescribed.prescriptionId()).orElseThrow().getStatus()).isEqualTo(
            PrescriptionStatus.PENDING_PAYMENT
        );

        assertThatThrownBy(() ->
            dispenseService.dispense(prescribed.prescriptionId(), handOver(dispenseLine(prescribed.lineId(), 5)))
        )
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("cannot be handed over")
            .hasMessageContaining("only once its bill is settled");

        assertThat(reloadDrug().getCurrentStock()).isEqualTo(100);
    }

    @Test
    void handingEverythingOverMarksThePrescriptionDispensed() {
        Prescribed prescribed = prescribe(5, true);

        PrescriptionViewDTO result = dispenseService.dispense(
            prescribed.prescriptionId(),
            handOver(dispenseLine(prescribed.lineId(), 5))
        );

        assertThat(result.getStatus()).isEqualTo(PrescriptionStatus.DISPENSED);
    }

    /** The reservation is finally discharged: the units leave the shelf and stop being promised. */
    @Test
    void handingOverTakesTheUnitsOffTheShelfAndDischargesTheReservation() {
        Prescribed prescribed = prescribe(5, true);
        Drug before = reloadDrug();
        assertThat(before.getReservedStock()).isEqualTo(5);
        assertThat(before.getCurrentStock()).isEqualTo(100);

        dispenseService.dispense(prescribed.prescriptionId(), handOver(dispenseLine(prescribed.lineId(), 5)));

        Drug after = reloadDrug();
        assertThat(after.getCurrentStock()).isEqualTo(95);
        assertThat(after.getReservedStock()).isZero();
    }

    @Test
    void aPartHandOverLeavesTheRestOutstanding() {
        Prescribed prescribed = prescribe(5, true);

        PrescriptionViewDTO first = dispenseService.dispense(
            prescribed.prescriptionId(),
            handOver(dispenseLine(prescribed.lineId(), 2))
        );

        assertThat(first.getStatus()).isEqualTo(PrescriptionStatus.PARTIALLY_DISPENSED);
        assertThat(reloadDrug().getCurrentStock()).isEqualTo(98);
        assertThat(reloadDrug().getReservedStock()).isEqualTo(3);

        PrescriptionViewDTO second = dispenseService.dispense(
            prescribed.prescriptionId(),
            handOver(dispenseLine(prescribed.lineId(), 3))
        );

        assertThat(second.getStatus()).isEqualTo(PrescriptionStatus.DISPENSED);
        assertThat(reloadDrug().getCurrentStock()).isEqualTo(95);
        assertThat(reloadDrug().getReservedStock()).isZero();
    }

    @Test
    void handingOverMoreThanRemainsIsRefused() {
        Prescribed prescribed = prescribe(5, true);
        dispenseService.dispense(prescribed.prescriptionId(), handOver(dispenseLine(prescribed.lineId(), 4)));

        assertThatThrownBy(() ->
            dispenseService.dispense(prescribed.prescriptionId(), handOver(dispenseLine(prescribed.lineId(), 2)))
        )
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("1 capsule still outstanding");

        // The refusal moved nothing: the units handed over earlier are the only ones gone.
        assertThat(reloadDrug().getCurrentStock()).isEqualTo(96);
        assertThat(reloadDrug().getReservedStock()).isEqualTo(1);
    }

    /**
     * Two entries for one line could each fit inside what remains while together exceeding it, which
     * would hand over more medicine than was ever prescribed.
     */
    @Test
    void theSameLineCannotBeListedTwiceInOneHandOver() {
        Prescribed prescribed = prescribe(5, true);

        assertThatThrownBy(() ->
            dispenseService.dispense(
                prescribed.prescriptionId(),
                handOver(dispenseLine(prescribed.lineId(), 3), dispenseLine(prescribed.lineId(), 3))
            )
        )
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("more than once");

        assertThat(reloadDrug().getCurrentStock()).isEqualTo(100);
    }

    @Test
    void aLineFromAnotherPrescriptionCannotBeHandedOver() {
        Prescribed first = prescribe(2, true);
        Prescribed second = prescribe(2, true);
        // Both prescriptions are holding their own reservations, and nothing has left the shelf yet.
        assertThat(reloadDrug().getCurrentStock()).isEqualTo(100);
        assertThat(reloadDrug().getReservedStock()).isEqualTo(4);

        assertThatThrownBy(() ->
            dispenseService.dispense(first.prescriptionId(), handOver(dispenseLine(second.lineId(), 1)))
        )
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("does not belong");

        // Refused before any stock moved, and both reservations are intact.
        assertThat(reloadDrug().getCurrentStock()).isEqualTo(100);
        assertThat(reloadDrug().getReservedStock()).isEqualTo(4);
    }

    /** The history is the only record of what physically left the shelf, so each hand-over must show. */
    @Test
    void theHistoryRecordsEachHandOverSeparately() {
        Prescribed prescribed = prescribe(5, true);
        dispenseService.dispense(prescribed.prescriptionId(), handOver(dispenseLine(prescribed.lineId(), 2)));

        DispenseRequestDTO second = handOver(dispenseLine(prescribed.lineId(), 3));
        second.setNote("Balance collected at the counter");
        dispenseService.dispense(prescribed.prescriptionId(), second);

        List<DispenseRecordDTO> history = dispenseService.history(prescribed.prescriptionId());

        assertThat(history).hasSize(2);
        assertThat(history)
            .extracting(record -> record.getItems().getFirst().quantity())
            .containsExactly(2, 3);
        assertThat(history.getFirst().getRecordedByLogin()).isEqualTo("admin");
        assertThat(history.getLast().getNote()).isEqualTo("Balance collected at the counter");
    }

    @Test
    void aWithdrawnPrescriptionCannotBeHandedOver() {
        Prescribed prescribed = prescribe(5, true);

        Prescription cancelled = prescriptionRepository.findById(prescribed.prescriptionId()).orElseThrow();
        cancelled.setStatus(PrescriptionStatus.CANCELLED);
        prescriptionRepository.save(cancelled);

        assertThatThrownBy(() -> dispenseService.dispense(prescribed.prescriptionId(), handOver(dispenseLine(prescribed.lineId(), 1))))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("never released");

        assertThat(reloadDrug().getCurrentStock()).isEqualTo(100);
    }

    // ---------------------------------------------------------------- helpers

    private record Prescribed(Long prescriptionId, Long lineId) {}

    /**
     * A prescription taken as far as the pharmacy queue when {@code paid} is set, and only as far as
     * waiting for payment when it is not.
     */
    private Prescribed prescribe(int quantity, boolean paid) {
        Visit visit = visitAwaitingDoctor();
        Long consultationId = consultationService.start(visit.getId(), new StartConsultationRequestDTO()).getId();

        PrescriptionLineRequestDTO line = new PrescriptionLineRequestDTO();
        line.setDrugId(drug.getId());
        line.setDosage("1 capsule three times daily");
        line.setDuration("7 days");
        line.setQuantity(quantity);

        PlacePrescriptionRequestDTO request = new PlacePrescriptionRequestDTO();
        request.setSource(PrescriptionSource.INTERNAL);
        request.setLines(List.of(line));
        PrescriptionViewDTO placed = prescriptionService.place(visit.getId(), request);

        consultationService.complete(consultationId, notes("Chest infection", null));

        if (paid) {
            Bill bill = billRepository.findById(visitRepository.findById(visit.getId()).orElseThrow().getBill().getId()).orElseThrow();
            RecordPaymentRequestDTO payment = new RecordPaymentRequestDTO();
            payment.setMethod(PaymentMethod.CASH);
            payment.setAmount(bill.getTotalAmount());
            payment.setReceiptNumber("RCPT-" + visit.getId());
            paymentService.recordPayment(visit.getId(), payment);
        }

        Prescription saved = prescriptionRepository.findById(placed.getPrescriptionId()).orElseThrow();
        Long lineId = prescriptionLineRepository.findWithDrugByPrescriptionId(saved.getId()).getFirst().getId();
        return new Prescribed(saved.getId(), lineId);
    }

    private Visit visitAwaitingDoctor() {
        VisitIntakeRequestDTO intake = new VisitIntakeRequestDTO();
        intake.setPatientId(patient.getId());
        intake.setType(VisitType.OUTPATIENT);
        intake.setPriority(VisitPriority.NORMAL);
        intake.setReasonForVisit("Dispense test");
        VisitDTO created = visitIntakeService.createVisit(intake);

        VitalsSubmissionRequestDTO vitals = new VitalsSubmissionRequestDTO();
        vitals.setTemperature(BigDecimal.valueOf(38.2));
        vitals.setPulseRate(88);
        vitals.setSystolicBp(122);
        vitals.setDiastolicBp(80);
        vitals.setOxygenSaturation(97);
        triageService.submitVitals(created.getId(), vitals);

        return visitRepository.findById(created.getId()).orElseThrow();
    }

    private static DispenseLineRequestDTO dispenseLine(Long prescriptionLineId, int quantity) {
        DispenseLineRequestDTO line = new DispenseLineRequestDTO();
        line.setPrescriptionLineId(prescriptionLineId);
        line.setQuantity(quantity);
        return line;
    }

    private static DispenseRequestDTO handOver(DispenseLineRequestDTO... lines) {
        DispenseRequestDTO request = new DispenseRequestDTO();
        request.setLines(List.of(lines));
        return request;
    }

    private static UpdateConsultationRequestDTO notes(String complaint, String findings) {
        UpdateConsultationRequestDTO request = new UpdateConsultationRequestDTO();
        request.setPresentingComplaint(complaint);
        request.setExaminationFindings(findings);
        return request;
    }

    private Drug reloadDrug() {
        return drugRepository.findById(drug.getId()).orElseThrow();
    }
}
