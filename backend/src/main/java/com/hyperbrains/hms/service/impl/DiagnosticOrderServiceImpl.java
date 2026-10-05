package com.hyperbrains.hms.service.impl;

import com.hyperbrains.hms.domain.DiagnosticOrder;
import com.hyperbrains.hms.repository.DiagnosticOrderRepository;
import com.hyperbrains.hms.service.BusinessRuleViolationException;
import com.hyperbrains.hms.service.DiagnosticOrderService;
import com.hyperbrains.hms.service.dto.DiagnosticOrderDTO;
import com.hyperbrains.hms.service.mapper.DiagnosticOrderMapper;
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
 * Service Implementation for managing {@link com.hyperbrains.hms.domain.DiagnosticOrder}.
 */
@Service
@Transactional
public class DiagnosticOrderServiceImpl implements DiagnosticOrderService {

    private static final Logger LOG = LoggerFactory.getLogger(DiagnosticOrderServiceImpl.class);

    private final DiagnosticOrderRepository diagnosticOrderRepository;

    private final DiagnosticOrderMapper diagnosticOrderMapper;

    public DiagnosticOrderServiceImpl(DiagnosticOrderRepository diagnosticOrderRepository, DiagnosticOrderMapper diagnosticOrderMapper) {
        this.diagnosticOrderRepository = diagnosticOrderRepository;
        this.diagnosticOrderMapper = diagnosticOrderMapper;
    }

    /**
     * Where the order stands, when it was placed, what kind it is, and the test name it copied when it was made.
     *
     * <p>{@code status} is what the result-entry path checks before it accepts a result, so setting it by hand is
     * how a result is filed against an order that was never placed or one that was already cancelled. {@code type}
     * and {@code orderedAt} are what the order was. And {@code testName} is a <em>copy</em>, taken when the order was
     * placed so that renaming the catalogue entry later cannot rewrite what was asked for — editing it here undoes
     * exactly that protection, which is why it is guarded twice.
     */
    private static final String[] WORKFLOW_OWNED_FIELDS = { "status", "orderedAt", "type", "testName" };

    @Override
    public DiagnosticOrderDTO save(DiagnosticOrderDTO diagnosticOrderDTO) {
        LOG.debug("Request to save DiagnosticOrder : {}", diagnosticOrderDTO);
        DiagnosticOrder diagnosticOrder = diagnosticOrderMapper.toEntity(diagnosticOrderDTO);
        diagnosticOrder = diagnosticOrderRepository.save(diagnosticOrder);
        return diagnosticOrderMapper.toDto(diagnosticOrder);
    }

    @Override
    public DiagnosticOrderDTO update(DiagnosticOrderDTO diagnosticOrderDTO) {
        LOG.debug("Request to update DiagnosticOrder : {}", diagnosticOrderDTO);
        // A PUT carries the whole record, so an empty field here means "clear it" rather than "leave it".
        refuseHandEdits(diagnosticOrderDTO, requireStored(diagnosticOrderDTO.getId()), false);
        DiagnosticOrder diagnosticOrder = diagnosticOrderMapper.toEntity(diagnosticOrderDTO);
        diagnosticOrder = diagnosticOrderRepository.save(diagnosticOrder);
        return diagnosticOrderMapper.toDto(diagnosticOrder);
    }

    @Override
    public Optional<DiagnosticOrderDTO> partialUpdate(DiagnosticOrderDTO diagnosticOrderDTO) {
        LOG.debug("Request to partially update DiagnosticOrder : {}", diagnosticOrderDTO);

        return diagnosticOrderRepository
            .findById(diagnosticOrderDTO.getId())
            .map(existingDiagnosticOrder -> {
                // A PATCH carries only what is changing, so an empty field here means "leave it".
                refuseHandEdits(diagnosticOrderDTO, existingDiagnosticOrder, true);
                diagnosticOrderMapper.partialUpdate(existingDiagnosticOrder, diagnosticOrderDTO);

                return existingDiagnosticOrder;
            })
            .map(diagnosticOrderRepository::save)
            .map(diagnosticOrderMapper::toDto);
    }

    private void refuseHandEdits(DiagnosticOrderDTO requested, DiagnosticOrder stored, boolean nullMeansUnchanged) {
        List<String> changed = WorkflowOwnedFields.changed(
            diagnosticOrderMapper.toEntity(requested),
            stored,
            nullMeansUnchanged,
            WORKFLOW_OWNED_FIELDS
        );
        if (!changed.isEmpty()) {
            throw BusinessRuleViolationException.of(
                "diagnosticOrderNotEditedByHand",
                "diagnosticOrder",
                "An order is placed and its result entered through their own operations; this request would change " +
                String.join(", ", changed)
            );
        }
    }

    private DiagnosticOrder requireStored(Long id) {
        return diagnosticOrderRepository
            .findById(id)
            .orElseThrow(() ->
                BusinessRuleViolationException.of("diagnosticOrderNotFound", "diagnosticOrder", "No diagnostic order with id " + id)
            );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DiagnosticOrderDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all DiagnosticOrders");
        return diagnosticOrderRepository.findAll(pageable).map(diagnosticOrderMapper::toDto);
    }

    public Page<DiagnosticOrderDTO> findAllWithEagerRelationships(Pageable pageable) {
        return diagnosticOrderRepository.findAllWithEagerRelationships(pageable).map(diagnosticOrderMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DiagnosticOrderDTO> findOne(Long id) {
        LOG.debug("Request to get DiagnosticOrder : {}", id);
        return diagnosticOrderRepository.findOneWithEagerRelationships(id).map(diagnosticOrderMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete DiagnosticOrder : {}", id);
        diagnosticOrderRepository.deleteById(id);
    }
}
