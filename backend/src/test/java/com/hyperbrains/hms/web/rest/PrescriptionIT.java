package com.hyperbrains.hms.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hyperbrains.hms.IntegrationTest;
import com.hyperbrains.hms.domain.Bill;
import com.hyperbrains.hms.domain.BillLineItem;
import com.hyperbrains.hms.domain.Consultation;
import com.hyperbrains.hms.domain.Drug;
import com.hyperbrains.hms.domain.Patient;
import com.hyperbrains.hms.domain.Prescription;
import com.hyperbrains.hms.domain.Visit;
import com.hyperbrains.hms.domain.enumeration.BillLineSourceType;
import com.hyperbrains.hms.domain.enumeration.PrescriptionSource;
import com.hyperbrains.hms.domain.enumeration.PrescriptionStatus;
import com.hyperbrains.hms.domain.enumeration.RegistrationStatus;
import com.hyperbrains.hms.domain.enumeration.Sex;
import com.hyperbrains.hms.domain.enumeration.VisitPriority;
import com.hyperbrains.hms.domain.enumeration.VisitStatus;
import com.hyperbrains.hms.domain.enumeration.VisitType;
import com.hyperbrains.hms.repository.AuditLogRepository;
import com.hyperbrains.hms.repository.BillLineItemRepository;
import com.hyperbrains.hms.repository.BillRepository;
import com.hyperbrains.hms.repository.ConsultationRepository;
import com.hyperbrains.hms.repository.DrugRepository;
import com.hyperbrains.hms.repository.HospitalServiceRepository;
import com.hyperbrains.hms.repository.PatientRepository;
import com.hyperbrains.hms.repository.PrescriptionLineRepository;
import com.hyperbrains.hms.repository.PrescriptionRepository;
import com.hyperbrains.hms.repository.VisitRepository;
import com.hyperbrains.hms.repository.VitalSignsRepository;
import com.hyperbrains.hms.service.AuditActions;
import com.hyperbrains.hms.service.BusinessRuleViolationException;
import com.hyperbrains.hms.service.HospitalIdService;
import com.hyperbrains.hms.service.dto.VisitDTO;
import com.hyperbrains.hms.service.dto.view.PlacePrescriptionRequestDTO;
import com.hyperbrains.hms.service.dto.view.PrescriptionBillableDTO;
import com.hyperbrains.hms.service.dto.view.PrescriptionLineRequestDTO;
import com.hyperbrains.hms.service.dto.view.PrescriptionLineViewDTO;
import com.hyperbrains.hms.service.dto.view.PrescriptionViewDTO;
import com.hyperbrains.hms.service.dto.view.StartConsultationRequestDTO;
import com.hyperbrains.hms.service.dto.view.UpdateConsultationRequestDTO;
import com.hyperbrains.hms.service.dto.view.VisitIntakeRequestDTO;
import com.hyperbrains.hms.service.dto.view.VitalsSubmissionRequestDTO;
import com.hyperbrains.hms.service.workflow.ConsultationWorkflowService;
import com.hyperbrains.hms.service.workflow.PrescriptionWorkflowService;
import com.hyperbrains.hms.service.workflow.TriageService;
import com.hyperbrains.hms.service.workflow.VisitIntakeService;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;

/**
 * Integration tests for prescribing, stock reservation and the payment-gated release to pharmacy.
 *
 * <p>The guarantee under test is the specification's payment-before-dispense rule: by the time a visit
 * reaches the payment stage every prescribed item has already been confirmed available, because the
 * stock was set aside when it was prescribed rather than merely checked.
 *
 * <p>The consultation fee is read from the catalogue rather than hard-coded, so changing the fee does
 * not mean chasing a number through the tests.
 */
@IntegrationTest
@WithMockUser(value = "admin", authorities = "ROLE_DOCTOR")
class PrescriptionIT {

    private static final BigDecimal PARACETAMOL_PRICE = new BigDecimal("2.50");

    private static final BigDecimal AMOXICILLIN_PRICE = new BigDecimal("5.00");

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
    private AuditLogRepository auditLogRepository;

    @Autowired
    private HospitalServiceRepository hospitalServiceRepository;

    @Autowired
    private HospitalIdService hospitalIdService;

    private Patient patient;

    private Drug paracetamol;

    private Drug amoxicillin;

    private Drug withdrawnDrug;

    @BeforeEach
    void setUp() {
        patient = new Patient();
        patient.setHospitalId(hospitalIdService.nextPermanentId());
        patient.setFullName("Prescription Test Patient");
        patient.setSex(Sex.FEMALE);
        patient.setSexEstimated(false);
        patient.setRegistrationStatus(RegistrationStatus.COMPLETE);
        patient = patientRepository.save(patient);

        paracetamol = drug("Paracetamol", "tablet", PARACETAMOL_PRICE, 100, 10, true);
        amoxicillin = drug("Amoxicillin", "capsule", AMOXICILLIN_PRICE, 10, 3, true);
        withdrawnDrug = drug("Withdrawn Ibuprofen", "tablet", new BigDecimal("1.00"), 50, 5, false);
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

        // Children before parents. A prescription line points at both its prescription and its drug,
        // and a visit points at its consultation and its bill.
        prescriptions.forEach(prescription -> {
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
        List.of(paracetamol, amoxicillin, withdrawnDrug)
            .stream()
            .filter(Objects::nonNull)
            .forEach(drug -> drugRepository.findById(drug.getId()).ifPresent(drugRepository::delete));
    }

    // ---------------------------------------------------------------- reservation

    @Test
    void prescribingSetsStockAsideWithoutTakingItOffTheShelf() {
        Visit visit = visitAwaitingDoctor();
        startConsultation(visit);

        prescriptionService.place(visit.getId(), internal(line(paracetamol, 10), line(amoxicillin, 4)));

        Drug paracetamolAfter = reload(paracetamol);
        assertThat(paracetamolAfter.getReservedStock()).isEqualTo(10);
        // The medicine is still physically the pharmacy's — only the promise changed.
        assertThat(paracetamolAfter.getCurrentStock()).isEqualTo(100);

        assertThat(reload(amoxicillin).getReservedStock()).isEqualTo(4);
        assertThat(reload(amoxicillin).getCurrentStock()).isEqualTo(10);
    }

    /**
     * Two prescribers reaching for the same drug is the case the reservation exists for: the second
     * one must be measured against what is left, not against the shelf count.
     */
    @Test
    void availabilityAccountsForWhatIsAlreadySetAside() {
        Visit first = visitAwaitingDoctor();
        startConsultation(first);
        // 10 capsules on the shelf, 8 set aside for this patient.
        prescriptionService.place(first.getId(), internal(line(amoxicillin, 8)));

        Visit second = visitAwaitingDoctor();
        startConsultation(second);

        assertThatThrownBy(() -> prescriptionService.place(second.getId(), internal(line(amoxicillin, 3))))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("Amoxicillin")
            .hasMessageContaining("1 short");

        // And the refusal did not take anything: 8 is still the only reservation.
        assertThat(reload(amoxicillin).getReservedStock()).isEqualTo(8);
    }

    /** A prescription is fully backed or it does not exist. */
    @Test
    void oneUnavailableLineRollsBackTheWholePrescription() {
        Visit visit = visitAwaitingDoctor();
        startConsultation(visit);

        assertThatThrownBy(() ->
            prescriptionService.place(visit.getId(), internal(line(paracetamol, 5), line(amoxicillin, 999)))
        )
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("Amoxicillin")
            .hasMessageContaining("available");

        // The first line's reservation must not survive a failure on the second.
        assertThat(reload(paracetamol).getReservedStock()).isZero();
        assertThat(reload(amoxicillin).getReservedStock()).isZero();
        assertThat(prescriptionRepository.findByVisitId(visit.getId())).isEmpty();
    }

    @Test
    void aWithdrawnDrugCannotBePrescribed() {
        Visit visit = visitAwaitingDoctor();
        startConsultation(visit);

        assertThatThrownBy(() -> prescriptionService.place(visit.getId(), internal(line(withdrawnDrug, 1))))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("no longer stocked");

        assertThat(reload(withdrawnDrug).getReservedStock()).isZero();
    }

    // ---------------------------------------------------------------- the charge

    @Test
    void prescribingChargesForEveryLineImmediately() {
        Visit visit = visitAwaitingDoctor();
        startConsultation(visit);

        PrescriptionViewDTO placed = prescriptionService.place(visit.getId(), internal(line(paracetamol, 10), line(amoxicillin, 4)));

        List<BillLineItem> lines = billLinesOf(visit.getId());
        assertThat(lines)
            .extracting(BillLineItem::getSourceType)
            .containsOnly(BillLineSourceType.PHARMACY);
        assertThat(lines)
            .extracting(BillLineItem::getDescription)
            .containsExactlyInAnyOrder("Paracetamol", "Amoxicillin");
        assertThat(lines)
            .filteredOn(lineItem -> lineItem.getDescription().equals("Paracetamol"))
            .singleElement()
            .satisfies(lineItem -> {
                assertThat(lineItem.getAmount()).isEqualByComparingTo(PARACETAMOL_PRICE.multiply(BigDecimal.TEN));
                // Keyed on the line, so a retry of the same prescription updates this row and not a new one.
                assertThat(lineItem.getSourceRef()).isEqualTo("PHARMACY:" + placed.getLines().getFirst().getLineId());
            });
    }

    @Test
    void theBillTotalIncludesMedicineOnceTheVisitReachesThePaymentStage() {
        Visit visit = visitAwaitingDoctor();
        Long consultationId = startConsultation(visit);
        prescriptionService.place(visit.getId(), internal(line(paracetamol, 10), line(amoxicillin, 4)));

        consultationService.complete(consultationId, notes("Sore throat", null));

        Visit reloaded = visitRepository.findById(visit.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(VisitStatus.WAITING_PAYMENT);

        BigDecimal consultationFee = hospitalServiceRepository.findOneByCode("CONSULTATION").orElseThrow().getPrice();
        BigDecimal expected = consultationFee.add(PARACETAMOL_PRICE.multiply(BigDecimal.TEN)).add(AMOXICILLIN_PRICE.multiply(new BigDecimal("4")));
        assertThat(billOf(visit.getId()).getTotalAmount()).isEqualByComparingTo(expected);
    }

    @Test
    void reachingThePaymentStageMovesThePrescriptionOutOfPendingNotOutOfTheQueue() {
        Visit visit = visitAwaitingDoctor();
        Long consultationId = startConsultation(visit);
        Long prescriptionId = prescriptionService.place(visit.getId(), internal(line(paracetamol, 2))).getPrescriptionId();
        assertThat(statusOf(prescriptionId)).isEqualTo(PrescriptionStatus.PENDING);

        consultationService.complete(consultationId, notes("Sore throat", null));

        // Part of what is owed now — but still not something pharmacy may hand over.
        assertThat(statusOf(prescriptionId)).isEqualTo(PrescriptionStatus.PENDING_PAYMENT);
        assertThat(queueIds()).doesNotContain(prescriptionId);
    }

    /**
     * A prescription written for a patient already at the desk joins the bill without reopening the
     * visit: there is nothing left to wait for, so it must not send them back a stage.
     */
    @Test
    void aLatePrescriptionJoinsTheBillWithoutReopeningTheVisit() {
        Visit visit = visitAwaitingDoctor();
        Long consultationId = startConsultation(visit);
        consultationService.complete(consultationId, notes("Nothing yet", null));
        assertThat(statusOf(visit)).isEqualTo(VisitStatus.WAITING_PAYMENT);

        Long prescriptionId = prescriptionService.place(visit.getId(), internal(line(paracetamol, 6))).getPrescriptionId();

        assertThat(statusOf(visit)).isEqualTo(VisitStatus.WAITING_PAYMENT);
        assertThat(statusOf(prescriptionId)).isEqualTo(PrescriptionStatus.PENDING_PAYMENT);
        BigDecimal consultationFee = hospitalServiceRepository.findOneByCode("CONSULTATION").orElseThrow().getPrice();
        assertThat(billOf(visit.getId()).getTotalAmount()).isEqualByComparingTo(
            consultationFee.add(PARACETAMOL_PRICE.multiply(new BigDecimal("6")))
        );
    }

    // ---------------------------------------------------------------- the walk-in

    /**
     * A pharmacy-only walk-in has no triage and no consultation, so nothing the derivation looks at
     * would ever fire: the charge itself is what makes the visit payable.
     */
    @Test
    void aPharmacyOnlyWalkInBecomesPayableAsSoonAsItIsCharged() {
        Visit visit = pharmacyOnlyVisit();
        assertThat(visit.getStatus()).isEqualTo(VisitStatus.REGISTERED);

        PrescriptionViewDTO placed = prescriptionService.place(
            visit.getId(),
            external("Nairobi Community Clinic", line(amoxicillin, 2))
        );

        assertThat(statusOf(visit)).isEqualTo(VisitStatus.WAITING_PAYMENT);
        assertThat(statusOf(placed.getPrescriptionId())).isEqualTo(PrescriptionStatus.PENDING_PAYMENT);
        assertThat(billOf(visit.getId()).getTotalAmount()).isEqualByComparingTo(AMOXICILLIN_PRICE.multiply(new BigDecimal("2")));
    }

    @Test
    void anOutsidePrescriptionNamesItsPrescriberAndIsNotAttributedToOurDoctor() {
        Visit visit = pharmacyOnlyVisit();

        PrescriptionViewDTO placed = prescriptionService.place(
            visit.getId(),
            external("Nairobi Community Clinic", line(amoxicillin, 1))
        );

        assertThat(placed.getPrescribingSource()).isEqualTo("Nairobi Community Clinic");
        // No doctor of ours wrote it, so attributing it to one would make the record untrue.
        assertThat(placed.getDoctorLogin()).isNull();
    }

    @Test
    void anOutsidePrescriptionWithoutAPrescriberIsRefused() {
        Visit visit = pharmacyOnlyVisit();

        assertThatThrownBy(() -> prescriptionService.place(visit.getId(), external(null, line(amoxicillin, 1))))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("must name the prescriber");

        assertThat(reload(amoxicillin).getReservedStock()).isZero();
    }

    @Test
    void anInternalPrescriptionIsAttributedToTheAuthenticatedDoctor() {
        Visit visit = visitAwaitingDoctor();
        startConsultation(visit);

        PrescriptionViewDTO placed = prescriptionService.place(visit.getId(), internal(line(paracetamol, 1)));

        assertThat(placed.getDoctorLogin()).isEqualTo("admin");
        assertThat(placed.getSource()).isEqualTo(PrescriptionSource.INTERNAL);
    }

    // ---------------------------------------------------------------- payment and the queue

    @Test
    void thePharmacyQueueStaysEmptyUntilTheBillIsPaid() {
        Visit visit = visitAwaitingDoctor();
        Long consultationId = startConsultation(visit);
        Long prescriptionId = prescriptionService.place(visit.getId(), internal(line(paracetamol, 3))).getPrescriptionId();
        consultationService.complete(consultationId, notes("Sore throat", null));

        assertThat(queueIds()).doesNotContain(prescriptionId);

        PrescriptionViewDTO released = prescriptionService.markPaid(prescriptionId);

        assertThat(released.getStatus()).isEqualTo(PrescriptionStatus.READY_FOR_DISPENSE);
        assertThat(queueIds()).contains(prescriptionId);
    }

    /** The payment step is retried, so arriving twice must not fail or re-queue anything. */
    @Test
    void markingAPrescriptionPaidTwiceIsHarmless() {
        Visit visit = visitAwaitingDoctor();
        Long consultationId = startConsultation(visit);
        Long prescriptionId = prescriptionService.place(visit.getId(), internal(line(paracetamol, 1))).getPrescriptionId();
        consultationService.complete(consultationId, notes("Sore throat", null));

        prescriptionService.markPaid(prescriptionId);
        PrescriptionViewDTO second = prescriptionService.markPaid(prescriptionId);

        assertThat(second.getStatus()).isEqualTo(PrescriptionStatus.READY_FOR_DISPENSE);
    }

    @Test
    void theQueueCarriesTheDrugsToHandOverAndWhoTheyBelongTo() {
        Visit visit = visitAwaitingDoctor();
        Long consultationId = startConsultation(visit);
        Long prescriptionId = prescriptionService.place(visit.getId(), internal(line(paracetamol, 3))).getPrescriptionId();
        consultationService.complete(consultationId, notes("Sore throat", null));
        prescriptionService.markPaid(prescriptionId);

        PrescriptionViewDTO queued = prescriptionService
            .pharmacyQueue(PageRequest.of(0, 50))
            .getContent()
            .stream()
            .filter(item -> item.getPrescriptionId().equals(prescriptionId))
            .findFirst()
            .orElseThrow();

        assertThat(queued.getPatient().getHospitalId()).isEqualTo(patient.getHospitalId());
        assertThat(queued.getLines())
            .singleElement()
            .satisfies(line -> {
                assertThat(line.getDrugName()).isEqualTo("Paracetamol");
                assertThat(line.getUnit()).isEqualTo("tablet");
                assertThat(line.getQuantity()).isEqualTo(3);
                assertThat(line.getDosage()).isEqualTo("1 tablet twice daily");
            });
    }

    // ---------------------------------------------------------------- the finance boundary

    /**
     * Finance needs to bill for medicine, not to read the prescription's clinical instructions.
     * Asserted structurally, so adding posology to the finance view fails here rather than in
     * production.
     */
    @Test
    void theFinanceViewCarriesPricesButNoPosology() {
        Visit visit = visitAwaitingDoctor();
        startConsultation(visit);
        prescriptionService.place(visit.getId(), internal(line(paracetamol, 10)));

        List<PrescriptionBillableDTO> billable = prescriptionService.billableForVisit(visit.getId());

        assertThat(billable)
            .singleElement()
            .satisfies(item -> {
                assertThat(item.getDrugName()).isEqualTo("Paracetamol");
                assertThat(item.getQuantity()).isEqualTo(10);
                assertThat(item.getUnitPrice()).isEqualByComparingTo(PARACETAMOL_PRICE);
                assertThat(item.getAmount()).isEqualByComparingTo(PARACETAMOL_PRICE.multiply(BigDecimal.TEN));
                assertThat(item.getStatus()).isEqualTo(PrescriptionStatus.PENDING);
            });

        assertThat(fieldNames(PrescriptionBillableDTO.class)).doesNotContain("dosage", "duration", "prescribingSource", "doctorLogin");
        // And the clinical view does carry the instructions, which is what makes that a decision.
        assertThat(fieldNames(PrescriptionViewDTO.class)).contains("lines");
        assertThat(fieldNames(PrescriptionLineViewDTO.class)).contains("dosage", "duration");
    }

    // ---------------------------------------------------------------- withdrawal

    @Test
    void prescribingRecordsWhenItWasWritten() {
        Visit visit = visitAwaitingDoctor();
        startConsultation(visit);

        PrescriptionViewDTO placed = prescriptionService.place(visit.getId(), internal(line(paracetamol, 1)));

        // Without this the dispensing queue cannot say how long a patient has been waiting.
        assertThat(placed.getCreatedAt()).isNotNull();
        assertThat(prescriptionRepository.findById(placed.getPrescriptionId()).orElseThrow().getCreatedAt()).isNotNull();
    }

    /**
     * The reason the whole withdrawal path exists: stock reserved for a patient who never comes back
     * would otherwise stay reserved for ever, and the availability figure would drift permanently
     * away from what is really on the shelf.
     */
    @Test
    void withdrawingAPrescriptionGivesTheStockBackAndTakesTheChargeOff() {
        Visit visit = visitAwaitingDoctor();
        startConsultation(visit);
        Long prescriptionId = prescriptionService.place(visit.getId(), internal(line(paracetamol, 10))).getPrescriptionId();
        assertThat(reload(paracetamol).getReservedStock()).isEqualTo(10);
        assertThat(billLinesOf(visit.getId())).hasSize(1);

        PrescriptionViewDTO cancelled = prescriptionService.cancel(prescriptionId, "Patient declined the medication");

        assertThat(cancelled.getStatus()).isEqualTo(PrescriptionStatus.CANCELLED);
        // The medicine never left the shelf, so only the promise is undone.
        assertThat(reload(paracetamol).getReservedStock()).isZero();
        assertThat(reload(paracetamol).getCurrentStock()).isEqualTo(100);
        // And the charge goes with it: nobody pays for medicine that will not be handed over.
        assertThat(billLinesOf(visit.getId())).isEmpty();
        assertThat(mustCancelReason(prescriptionId)).isEqualTo("Patient declined the medication");
    }

    /** A world where the visit already waited on payment is the case the derivation alone gets wrong. */
    @Test
    void withdrawingUpdatesTheBillTotalEvenWhenTheVisitIsAlreadyAtPayment() {
        Visit visit = visitAwaitingDoctor();
        Long consultationId = startConsultation(visit);
        Long prescriptionId = prescriptionService.place(visit.getId(), internal(line(paracetamol, 10))).getPrescriptionId();
        consultationService.complete(consultationId, notes("Sore throat", null));

        BigDecimal consultationFee = hospitalServiceRepository.findOneByCode("CONSULTATION").orElseThrow().getPrice();
        assertThat(billOf(visit.getId()).getTotalAmount()).isEqualByComparingTo(
            consultationFee.add(PARACETAMOL_PRICE.multiply(BigDecimal.TEN))
        );

        prescriptionService.cancel(prescriptionId, "Out of stock at the counter");

        // The status does not change, so only an explicit re-total gets the money right.
        assertThat(statusOf(visit)).isEqualTo(VisitStatus.WAITING_PAYMENT);
        assertThat(billOf(visit.getId()).getTotalAmount()).isEqualByComparingTo(consultationFee);
    }

    @Test
    void withdrawingRequiresAReason() {
        Visit visit = visitAwaitingDoctor();
        startConsultation(visit);
        Long prescriptionId = prescriptionService.place(visit.getId(), internal(line(paracetamol, 4))).getPrescriptionId();

        assertThatThrownBy(() -> prescriptionService.cancel(prescriptionId, "   "))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("requires a reason");

        // Nothing moved: the stock is still set aside and the charge still stands.
        assertThat(reload(paracetamol).getReservedStock()).isEqualTo(4);
        assertThat(statusOf(prescriptionId)).isEqualTo(PrescriptionStatus.PENDING);
    }

    /** Once the money is taken the medicine is owed, so undoing it is a refund, not a cancellation. */
    @Test
    void aPrescriptionCannotBeWithdrawnAfterItIsPaid() {
        Visit visit = visitAwaitingDoctor();
        Long consultationId = startConsultation(visit);
        Long prescriptionId = prescriptionService.place(visit.getId(), internal(line(paracetamol, 2))).getPrescriptionId();
        consultationService.complete(consultationId, notes("Sore throat", null));
        prescriptionService.markPaid(prescriptionId);

        assertThatThrownBy(() -> prescriptionService.cancel(prescriptionId, "Changed my mind"))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("cannot be withdrawn");

        // The reservation stays, because the patient has paid for that medicine.
        assertThat(reload(paracetamol).getReservedStock()).isEqualTo(2);
        assertThat(statusOf(prescriptionId)).isEqualTo(PrescriptionStatus.READY_FOR_DISPENSE);
    }

    /** Partly handed over means some of that stock is no longer on the shelf to give back. */
    @Test
    void aPrescriptionCannotBeWithdrawnOnceAnyOfItHasBeenHandedOver() {
        Visit visit = visitAwaitingDoctor();
        Long consultationId = startConsultation(visit);
        Long prescriptionId = prescriptionService.place(visit.getId(), internal(line(paracetamol, 3))).getPrescriptionId();
        consultationService.complete(consultationId, notes("Sore throat", null));
        prescriptionService.markPaid(prescriptionId);

        Prescription partiallyDispensed = prescriptionRepository.findById(prescriptionId).orElseThrow();
        partiallyDispensed.setStatus(PrescriptionStatus.PARTIALLY_DISPENSED);
        prescriptionRepository.save(partiallyDispensed);

        assertThatThrownBy(() -> prescriptionService.cancel(prescriptionId, "Recalled"))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("cannot be withdrawn");

        assertThat(reload(paracetamol).getReservedStock()).isEqualTo(3);
    }

    /** Withdrawing twice must not release the same stock twice. */
    @Test
    void withdrawingTwiceIsRefusedAndDoesNotDoubleRelease() {
        Visit visit = visitAwaitingDoctor();
        startConsultation(visit);
        Long prescriptionId = prescriptionService.place(visit.getId(), internal(line(paracetamol, 6))).getPrescriptionId();
        prescriptionService.cancel(prescriptionId, "First and only withdrawal");

        assertThatThrownBy(() -> prescriptionService.cancel(prescriptionId, "Trying again"))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("cannot be withdrawn");

        // reservedStock of -6 would make availability lie in the dangerous direction.
        assertThat(reload(paracetamol).getReservedStock()).isZero();
    }

    // ---------------------------------------------------------------- guards

    @Test
    void aPrescriptionCannotBeWrittenOnAClosedVisit() {
        Visit visit = visitAwaitingDoctor();
        startConsultation(visit);

        Visit closed = visitRepository.findById(visit.getId()).orElseThrow();
        closed.setStatus(VisitStatus.CLOSED);
        visitRepository.save(closed);

        assertThatThrownBy(() -> prescriptionService.place(visit.getId(), internal(line(paracetamol, 1))))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("no longer accepts clinical work");
    }

    // ---------------------------------------------------------------- helpers

    private Visit visitAwaitingDoctor() {
        VisitIntakeRequestDTO intake = intakeRequest(VisitType.OUTPATIENT);
        VisitDTO created = visitIntakeService.createVisit(intake);

        VitalsSubmissionRequestDTO vitals = new VitalsSubmissionRequestDTO();
        vitals.setTemperature(BigDecimal.valueOf(37.0));
        vitals.setPulseRate(78);
        vitals.setSystolicBp(119);
        vitals.setDiastolicBp(77);
        vitals.setOxygenSaturation(98);
        triageService.submitVitals(created.getId(), vitals);

        return visitRepository.findById(created.getId()).orElseThrow();
    }

    private Visit pharmacyOnlyVisit() {
        VisitDTO created = visitIntakeService.createVisit(intakeRequest(VisitType.PHARMACY_ONLY));
        return visitRepository.findById(created.getId()).orElseThrow();
    }

    private VisitIntakeRequestDTO intakeRequest(VisitType type) {
        VisitIntakeRequestDTO intake = new VisitIntakeRequestDTO();
        intake.setPatientId(patient.getId());
        intake.setType(type);
        intake.setPriority(VisitPriority.NORMAL);
        intake.setReasonForVisit("Prescription test");
        return intake;
    }

    private Long startConsultation(Visit visit) {
        return consultationService.start(visit.getId(), new StartConsultationRequestDTO()).getId();
    }

    private static PrescriptionLineRequestDTO line(Drug drug, int quantity) {
        PrescriptionLineRequestDTO request = new PrescriptionLineRequestDTO();
        request.setDrugId(drug.getId());
        request.setDosage("1 tablet twice daily");
        request.setDuration("5 days");
        request.setQuantity(quantity);
        return request;
    }

    private static PlacePrescriptionRequestDTO internal(PrescriptionLineRequestDTO... lines) {
        PlacePrescriptionRequestDTO request = new PlacePrescriptionRequestDTO();
        request.setSource(PrescriptionSource.INTERNAL);
        request.setLines(List.of(lines));
        return request;
    }

    private static PlacePrescriptionRequestDTO external(String prescribingSource, PrescriptionLineRequestDTO... lines) {
        PlacePrescriptionRequestDTO request = new PlacePrescriptionRequestDTO();
        request.setSource(PrescriptionSource.EXTERNAL);
        request.setPrescribingSource(prescribingSource);
        request.setLines(List.of(lines));
        return request;
    }

    private static UpdateConsultationRequestDTO notes(String complaint, String findings) {
        UpdateConsultationRequestDTO request = new UpdateConsultationRequestDTO();
        request.setPresentingComplaint(complaint);
        request.setExaminationFindings(findings);
        return request;
    }

    private Drug reload(Drug drug) {
        return drugRepository.findById(drug.getId()).orElseThrow();
    }

    /** The reason recorded on the withdrawal, read back from the audit trail rather than assumed. */
    private String mustCancelReason(Long prescriptionId) {
        return auditLogRepository
            .findByEntityNameAndEntityIdOrderByIdAsc("Prescription", String.valueOf(prescriptionId))
            .stream()
            .filter(entry -> AuditActions.PRESCRIPTION_CANCELLED.equals(entry.getAction()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("No PRESCRIPTION_CANCELLED audit entry for prescription " + prescriptionId))
            .getReason();
    }

    private VisitStatus statusOf(Visit visit) {
        return visitRepository.findById(visit.getId()).orElseThrow().getStatus();
    }

    private PrescriptionStatus statusOf(Long prescriptionId) {
        return prescriptionRepository.findById(prescriptionId).orElseThrow().getStatus();
    }

    private List<Long> queueIds() {
        return prescriptionService
            .pharmacyQueue(PageRequest.of(0, 100))
            .getContent()
            .stream()
            .map(PrescriptionViewDTO::getPrescriptionId)
            .toList();
    }

    private List<BillLineItem> billLinesOf(Long visitId) {
        return billLineItemRepository.findByBillIdOrderByIdAsc(billOf(visitId).getId());
    }

    private Bill billOf(Long visitId) {
        // The visit's association is lazy and these tests run outside a session, so the bill is read
        // back by id rather than touched through the visit.
        return billRepository.findById(visitRepository.findById(visitId).orElseThrow().getBill().getId()).orElseThrow();
    }

    private Drug drug(String name, String unit, BigDecimal price, int stock, int threshold, boolean active) {
        Drug drug = new Drug();
        drug.setName(name);
        drug.setUnit(unit);
        drug.setPrice(price);
        drug.setCurrentStock(stock);
        drug.setReservedStock(0);
        drug.setLowStockThreshold(threshold);
        drug.setActive(active);
        return drugRepository.save(drug);
    }

    private static List<String> fieldNames(Class<?> type) {
        return Stream.concat(Arrays.stream(type.getDeclaredFields()), Arrays.stream(type.getSuperclass().getDeclaredFields()))
            .map(Field::getName)
            .toList();
    }
}
