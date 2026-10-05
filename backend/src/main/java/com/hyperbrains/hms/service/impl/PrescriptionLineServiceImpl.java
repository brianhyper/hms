package com.hyperbrains.hms.service.impl;

import com.hyperbrains.hms.domain.PrescriptionLine;
import com.hyperbrains.hms.repository.PrescriptionLineRepository;
import com.hyperbrains.hms.service.BusinessRuleViolationException;
import com.hyperbrains.hms.service.PrescriptionLineService;
import com.hyperbrains.hms.service.dto.PrescriptionLineDTO;
import com.hyperbrains.hms.service.mapper.PrescriptionLineMapper;
import com.hyperbrains.hms.service.rules.WorkflowOwnedFields;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.hyperbrains.hms.domain.PrescriptionLine}.
 */
@Service
@Transactional
public class PrescriptionLineServiceImpl implements PrescriptionLineService {

    private static final Logger LOG = LoggerFactory.getLogger(PrescriptionLineServiceImpl.class);

    private final PrescriptionLineRepository prescriptionLineRepository;

    private final PrescriptionLineMapper prescriptionLineMapper;

    public PrescriptionLineServiceImpl(
        PrescriptionLineRepository prescriptionLineRepository,
        PrescriptionLineMapper prescriptionLineMapper
    ) {
        this.prescriptionLineRepository = prescriptionLineRepository;
        this.prescriptionLineMapper = prescriptionLineMapper;
    }

    /**
     * What is to be given, how much, and for how long — the content of a prescriber's decision.
     *
     * <p>On this entity the clinical content <em>is</em> the state: there is no separate status column, so the dosage,
     * the duration and the quantity are what the pharmacy dispenses against and what the ward administers. Changing
     * any of them by hand changes what the doctor decided, with nobody's name against the change — and unlike a
     * correction it leaves no record that a different amount was ever prescribed.
     *
     * <p>The drug and the prescription it hangs off are deliberately not in this list; see
     * {@code WardCoverServiceImpl} for why references are left to the guard that compares by id.
     */
    private static final String[] WORKFLOW_OWNED_FIELDS = { "dosage", "duration", "quantity" };

    @Override
    public PrescriptionLineDTO save(PrescriptionLineDTO prescriptionLineDTO) {
        LOG.debug("Request to save PrescriptionLine : {}", prescriptionLineDTO);
        PrescriptionLine prescriptionLine = prescriptionLineMapper.toEntity(prescriptionLineDTO);
        prescriptionLine = prescriptionLineRepository.save(prescriptionLine);
        return prescriptionLineMapper.toDto(prescriptionLine);
    }

    @Override
    public PrescriptionLineDTO update(PrescriptionLineDTO prescriptionLineDTO) {
        LOG.debug("Request to update PrescriptionLine : {}", prescriptionLineDTO);
        // A PUT carries the whole record, so an empty field here means "clear it" rather than "leave it".
        refuseHandEdits(prescriptionLineDTO, requireStored(prescriptionLineDTO.getId()), false);
        PrescriptionLine prescriptionLine = prescriptionLineMapper.toEntity(prescriptionLineDTO);
        prescriptionLine = prescriptionLineRepository.save(prescriptionLine);
        return prescriptionLineMapper.toDto(prescriptionLine);
    }

    @Override
    public Optional<PrescriptionLineDTO> partialUpdate(PrescriptionLineDTO prescriptionLineDTO) {
        LOG.debug("Request to partially update PrescriptionLine : {}", prescriptionLineDTO);

        return prescriptionLineRepository
            .findById(prescriptionLineDTO.getId())
            .map(existingPrescriptionLine -> {
                // A PATCH carries only what is changing, so an empty field here means "leave it".
                refuseHandEdits(prescriptionLineDTO, existingPrescriptionLine, true);
                prescriptionLineMapper.partialUpdate(existingPrescriptionLine, prescriptionLineDTO);

                return existingPrescriptionLine;
            })
            .map(prescriptionLineRepository::save)
            .map(prescriptionLineMapper::toDto);
    }

    private void refuseHandEdits(PrescriptionLineDTO requested, PrescriptionLine stored, boolean nullMeansUnchanged) {
        List<String> changed = WorkflowOwnedFields.changed(
            prescriptionLineMapper.toEntity(requested),
            stored,
            nullMeansUnchanged,
            WORKFLOW_OWNED_FIELDS
        );
        if (!changed.isEmpty()) {
            throw BusinessRuleViolationException.of(
                "prescriptionLineNotEditedByHand",
                "prescriptionLine",
                "What a prescriber decided is changed by a new prescription, not by an edit; this request would change " +
                String.join(", ", changed)
            );
        }
    }

    private PrescriptionLine requireStored(Long id) {
        return prescriptionLineRepository
            .findById(id)
            .orElseThrow(() ->
                BusinessRuleViolationException.of("prescriptionLineNotFound", "prescriptionLine", "No prescription line with id " + id)
            );
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionLineDTO> findAll() {
        LOG.debug("Request to get all PrescriptionLines");
        return prescriptionLineRepository
            .findAll()
            .stream()
            .map(prescriptionLineMapper::toDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PrescriptionLineDTO> findOne(Long id) {
        LOG.debug("Request to get PrescriptionLine : {}", id);
        return prescriptionLineRepository.findById(id).map(prescriptionLineMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete PrescriptionLine : {}", id);
        prescriptionLineRepository.deleteById(id);
    }
}
