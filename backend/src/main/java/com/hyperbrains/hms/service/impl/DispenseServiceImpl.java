package com.hyperbrains.hms.service.impl;

import com.hyperbrains.hms.domain.Dispense;
import com.hyperbrains.hms.repository.DispenseRepository;
import com.hyperbrains.hms.service.BusinessRuleViolationException;
import com.hyperbrains.hms.service.DispenseService;
import com.hyperbrains.hms.service.dto.DispenseDTO;
import com.hyperbrains.hms.service.mapper.DispenseMapper;
import com.hyperbrains.hms.service.rules.WorkflowOwnedFields;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.hyperbrains.hms.domain.Dispense}.
 */
@Service
@Transactional
public class DispenseServiceImpl implements DispenseService {

    private static final Logger LOG = LoggerFactory.getLogger(DispenseServiceImpl.class);

    private final DispenseRepository dispenseRepository;

    private final DispenseMapper dispenseMapper;

    public DispenseServiceImpl(DispenseRepository dispenseRepository, DispenseMapper dispenseMapper) {
        this.dispenseRepository = dispenseRepository;
        this.dispenseMapper = dispenseMapper;
    }

    /**
     * When the medicine left the counter, which is written by handing it over and must not be written by an update.
     *
     * <p>The time a drug was dispensed is what a later reader compares against the prescription and against what the
     * ward recorded as given. Moving it by hand makes the pharmacy's record agree with a story told afterwards,
     * which is the one kind of change nothing downstream can detect.
     */
    private static final String[] WORKFLOW_OWNED_FIELDS = { "dispensedAt" };

    @Override
    public DispenseDTO save(DispenseDTO dispenseDTO) {
        LOG.debug("Request to save Dispense : {}", dispenseDTO);
        Dispense dispense = dispenseMapper.toEntity(dispenseDTO);
        dispense = dispenseRepository.save(dispense);
        return dispenseMapper.toDto(dispense);
    }

    @Override
    public DispenseDTO update(DispenseDTO dispenseDTO) {
        LOG.debug("Request to update Dispense : {}", dispenseDTO);
        // A PUT carries the whole record, so an empty field here means "clear it" rather than "leave it".
        refuseHandEdits(dispenseDTO, requireStored(dispenseDTO.getId()), false);
        Dispense dispense = dispenseMapper.toEntity(dispenseDTO);
        dispense = dispenseRepository.save(dispense);
        return dispenseMapper.toDto(dispense);
    }

    @Override
    public Optional<DispenseDTO> partialUpdate(DispenseDTO dispenseDTO) {
        LOG.debug("Request to partially update Dispense : {}", dispenseDTO);

        return dispenseRepository
            .findById(dispenseDTO.getId())
            .map(existingDispense -> {
                // A PATCH carries only what is changing, so an empty field here means "leave it".
                refuseHandEdits(dispenseDTO, existingDispense, true);
                dispenseMapper.partialUpdate(existingDispense, dispenseDTO);

                return existingDispense;
            })
            .map(dispenseRepository::save)
            .map(dispenseMapper::toDto);
    }

    private void refuseHandEdits(DispenseDTO requested, Dispense stored, boolean nullMeansUnchanged) {
        List<String> changed = WorkflowOwnedFields.changed(
            dispenseMapper.toEntity(requested),
            stored,
            nullMeansUnchanged,
            WORKFLOW_OWNED_FIELDS
        );
        if (!changed.isEmpty()) {
            throw BusinessRuleViolationException.of(
                "dispenseNotEditedByHand",
                "dispense",
                "A dispense records medicine that was handed over, and when, and is not edited; this request would change " +
                String.join(", ", changed)
            );
        }
    }

    private Dispense requireStored(Long id) {
        return dispenseRepository
            .findById(id)
            .orElseThrow(() -> BusinessRuleViolationException.of("dispenseNotFound", "dispense", "No dispense with id " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DispenseDTO> findAll() {
        LOG.debug("Request to get all Dispenses");
        return dispenseRepository.findAll().stream().map(dispenseMapper::toDto).collect(Collectors.toCollection(LinkedList::new));
    }

    public Page<DispenseDTO> findAllWithEagerRelationships(Pageable pageable) {
        return dispenseRepository.findAllWithEagerRelationships(pageable).map(dispenseMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DispenseDTO> findOne(Long id) {
        LOG.debug("Request to get Dispense : {}", id);
        return dispenseRepository.findOneWithEagerRelationships(id).map(dispenseMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Dispense : {}", id);
        dispenseRepository.deleteById(id);
    }
}
