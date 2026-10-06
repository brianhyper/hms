package com.hyperbrains.hms.service.impl;

import com.hyperbrains.hms.domain.PrescriptionLine;
import com.hyperbrains.hms.repository.PrescriptionLineRepository;
import com.hyperbrains.hms.service.BusinessRuleViolationException;
import com.hyperbrains.hms.service.PrescriptionLineService;
import com.hyperbrains.hms.service.dto.PrescriptionLineDTO;
import com.hyperbrains.hms.service.mapper.PrescriptionLineMapper;
import com.hyperbrains.hms.service.rules.WorkflowOwnedFields;
import java.util.ArrayList;
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
     * <p>The drug and the prescription it hangs off are guarded too, but separately and by id rather than by field
     * name: a reference cannot go in this list, because the stored one is a proxy and this guard compares by identity.
     * See {@link WorkflowOwnedFields#referenceChanged}.
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
        List<String> changed = new ArrayList<>(
            WorkflowOwnedFields.changed(
                prescriptionLineMapper.toEntity(requested),
                stored,
                nullMeansUnchanged,
                WORKFLOW_OWNED_FIELDS
            )
        );

        // The references are compared by id, not as entities: the stored side is a Hibernate proxy and reading an id
        // off it forces a load. The request carries the id as a plain value and the stored ids come from a scalar
        // projection, so neither side touches a proxy and a legitimate edit that re-sends the same drug is not
        // mistaken for a re-point.
        PrescriptionLineRepository.ReferenceIds storedIds = prescriptionLineRepository
            .findReferenceIds(stored.getId())
            .orElseThrow(() ->
                BusinessRuleViolationException.of(
                    "prescriptionLineNotFound",
                    "prescriptionLine",
                    "No prescription line with id " + stored.getId()
                )
            );
        if (WorkflowOwnedFields.referenceChanged(idOfDrug(requested), storedIds.getDrugId(), nullMeansUnchanged)) {
            changed.add("drug");
        }
        if (WorkflowOwnedFields.referenceChanged(idOfPrescription(requested), storedIds.getPrescriptionId(), nullMeansUnchanged)) {
            changed.add("prescription");
        }

        if (!changed.isEmpty()) {
            throw BusinessRuleViolationException.of(
                "prescriptionLineNotEditedByHand",
                "prescriptionLine",
                "What a prescriber decided is changed by a new prescription, not by an edit; this request would change " +
                String.join(", ", changed)
            );
        }
    }

    /** The id the request names for the drug, or null when it names none. */
    private static Long idOfDrug(PrescriptionLineDTO requested) {
        return requested.getDrug() == null ? null : requested.getDrug().getId();
    }

    /** The id the request names for the prescription, or null when it names none. */
    private static Long idOfPrescription(PrescriptionLineDTO requested) {
        return requested.getPrescription() == null ? null : requested.getPrescription().getId();
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
