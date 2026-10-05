package com.hyperbrains.hms.service.impl;

import com.hyperbrains.hms.domain.Prescription;
import com.hyperbrains.hms.repository.PrescriptionRepository;
import com.hyperbrains.hms.service.BusinessRuleViolationException;
import com.hyperbrains.hms.service.PrescriptionService;
import com.hyperbrains.hms.service.dto.PrescriptionDTO;
import com.hyperbrains.hms.service.mapper.PrescriptionMapper;
import com.hyperbrains.hms.service.rules.WorkflowOwnedFields;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.hyperbrains.hms.domain.Prescription}.
 */
@Service
@Transactional
public class PrescriptionServiceImpl implements PrescriptionService {

    private static final Logger LOG = LoggerFactory.getLogger(PrescriptionServiceImpl.class);

    private final PrescriptionRepository prescriptionRepository;

    private final PrescriptionMapper prescriptionMapper;

    public PrescriptionServiceImpl(PrescriptionRepository prescriptionRepository, PrescriptionMapper prescriptionMapper) {
        this.prescriptionRepository = prescriptionRepository;
        this.prescriptionMapper = prescriptionMapper;
    }

    /**
     * Where the prescription stands, and when it was placed, both owned by the prescribing operations.
     *
     * <p>{@code status} is what the pharmacy queue, the billing side and the dispense check all read: a prescription
     * set to dispensed by hand is one the medicine can be handed over against twice, and one set back to pending is
     * medicine that can be handed over again at all. {@code createdAt} is when the prescriber placed it, which is
     * what makes a prescription reviewable against the notes and the visit it belongs to.
     */
    private static final String[] WORKFLOW_OWNED_FIELDS = { "status", "createdAt" };

    @Override
    public PrescriptionDTO save(PrescriptionDTO prescriptionDTO) {
        LOG.debug("Request to save Prescription : {}", prescriptionDTO);
        Prescription prescription = prescriptionMapper.toEntity(prescriptionDTO);
        prescription = prescriptionRepository.save(prescription);
        return prescriptionMapper.toDto(prescription);
    }

    @Override
    public PrescriptionDTO update(PrescriptionDTO prescriptionDTO) {
        LOG.debug("Request to update Prescription : {}", prescriptionDTO);
        // A PUT carries the whole record, so an empty field here means "clear it" rather than "leave it".
        refuseHandEdits(prescriptionDTO, requireStored(prescriptionDTO.getId()), false);
        Prescription prescription = prescriptionMapper.toEntity(prescriptionDTO);
        prescription = prescriptionRepository.save(prescription);
        return prescriptionMapper.toDto(prescription);
    }

    @Override
    public Optional<PrescriptionDTO> partialUpdate(PrescriptionDTO prescriptionDTO) {
        LOG.debug("Request to partially update Prescription : {}", prescriptionDTO);

        return prescriptionRepository
            .findById(prescriptionDTO.getId())
            .map(existingPrescription -> {
                // A PATCH carries only what is changing, so an empty field here means "leave it".
                refuseHandEdits(prescriptionDTO, existingPrescription, true);
                prescriptionMapper.partialUpdate(existingPrescription, prescriptionDTO);

                return existingPrescription;
            })
            .map(prescriptionRepository::save)
            .map(prescriptionMapper::toDto);
    }

    private void refuseHandEdits(PrescriptionDTO requested, Prescription stored, boolean nullMeansUnchanged) {
        List<String> changed = WorkflowOwnedFields.changed(
            prescriptionMapper.toEntity(requested),
            stored,
            nullMeansUnchanged,
            WORKFLOW_OWNED_FIELDS
        );
        if (!changed.isEmpty()) {
            throw BusinessRuleViolationException.of(
                "prescriptionNotEditedByHand",
                "prescription",
                "A prescription is placed, dispensed or cancelled by its own operations; this request would change " +
                String.join(", ", changed)
            );
        }
    }

    private Prescription requireStored(Long id) {
        return prescriptionRepository
            .findById(id)
            .orElseThrow(() -> BusinessRuleViolationException.of("prescriptionNotFound", "prescription", "No prescription with id " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PrescriptionDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Prescriptions");
        return prescriptionRepository.findAll(pageable).map(prescriptionMapper::toDto);
    }

    public Page<PrescriptionDTO> findAllWithEagerRelationships(Pageable pageable) {
        return prescriptionRepository.findAllWithEagerRelationships(pageable).map(prescriptionMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PrescriptionDTO> findOne(Long id) {
        LOG.debug("Request to get Prescription : {}", id);
        return prescriptionRepository.findOneWithEagerRelationships(id).map(prescriptionMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Prescription : {}", id);
        prescriptionRepository.deleteById(id);
    }
}
