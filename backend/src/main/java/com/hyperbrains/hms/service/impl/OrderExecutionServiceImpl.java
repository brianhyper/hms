package com.hyperbrains.hms.service.impl;

import com.hyperbrains.hms.domain.OrderExecution;
import com.hyperbrains.hms.repository.OrderExecutionRepository;
import com.hyperbrains.hms.service.BusinessRuleViolationException;
import com.hyperbrains.hms.service.OrderExecutionService;
import com.hyperbrains.hms.service.dto.OrderExecutionDTO;
import com.hyperbrains.hms.service.mapper.OrderExecutionMapper;
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
 * Service Implementation for managing {@link com.hyperbrains.hms.domain.OrderExecution}.
 */
@Service
@Transactional
public class OrderExecutionServiceImpl implements OrderExecutionService {

    private static final Logger LOG = LoggerFactory.getLogger(OrderExecutionServiceImpl.class);

    private final OrderExecutionRepository orderExecutionRepository;

    private final OrderExecutionMapper orderExecutionMapper;

    public OrderExecutionServiceImpl(OrderExecutionRepository orderExecutionRepository, OrderExecutionMapper orderExecutionMapper) {
        this.orderExecutionRepository = orderExecutionRepository;
        this.orderExecutionMapper = orderExecutionMapper;
    }

    /**
     * When the dose was given, which is written by recording the execution and must not be written by an update.
     *
     * <p>The time of an execution is clinical evidence: it is what a later reader uses to tell whether a drug was
     * given, and when relative to the next one. Changing it by hand rewrites what was observed — and unlike a
     * correction, which leaves both the original and the amendment, it leaves no trace at all. The way to record a
     * correction is the workflow operation, which keeps the record of what it changed.
     */
    private static final String[] WORKFLOW_OWNED_FIELDS = { "executedAt" };

    @Override
    public OrderExecutionDTO save(OrderExecutionDTO orderExecutionDTO) {
        LOG.debug("Request to save OrderExecution : {}", orderExecutionDTO);
        OrderExecution orderExecution = orderExecutionMapper.toEntity(orderExecutionDTO);
        orderExecution = orderExecutionRepository.save(orderExecution);
        return orderExecutionMapper.toDto(orderExecution);
    }

    @Override
    public OrderExecutionDTO update(OrderExecutionDTO orderExecutionDTO) {
        LOG.debug("Request to update OrderExecution : {}", orderExecutionDTO);
        // A PUT carries the whole record, so an empty field here means "clear it" rather than "leave it".
        refuseHandEdits(orderExecutionDTO, requireStored(orderExecutionDTO.getId()), false);
        OrderExecution orderExecution = orderExecutionMapper.toEntity(orderExecutionDTO);
        orderExecution = orderExecutionRepository.save(orderExecution);
        return orderExecutionMapper.toDto(orderExecution);
    }

    @Override
    public Optional<OrderExecutionDTO> partialUpdate(OrderExecutionDTO orderExecutionDTO) {
        LOG.debug("Request to partially update OrderExecution : {}", orderExecutionDTO);

        return orderExecutionRepository
            .findById(orderExecutionDTO.getId())
            .map(existingOrderExecution -> {
                // A PATCH carries only what is changing, so an empty field here means "leave it".
                refuseHandEdits(orderExecutionDTO, existingOrderExecution, true);
                orderExecutionMapper.partialUpdate(existingOrderExecution, orderExecutionDTO);

                return existingOrderExecution;
            })
            .map(orderExecutionRepository::save)
            .map(orderExecutionMapper::toDto);
    }

    private void refuseHandEdits(OrderExecutionDTO requested, OrderExecution stored, boolean nullMeansUnchanged) {
        List<String> changed = WorkflowOwnedFields.changed(
            orderExecutionMapper.toEntity(requested),
            stored,
            nullMeansUnchanged,
            WORKFLOW_OWNED_FIELDS
        );
        if (!changed.isEmpty()) {
            throw BusinessRuleViolationException.of(
                "orderExecutionNotEditedByHand",
                "orderExecution",
                "An execution is recorded by carrying it out, and the time it was given is not edited; this request would change " +
                String.join(", ", changed)
            );
        }
    }

    private OrderExecution requireStored(Long id) {
        return orderExecutionRepository
            .findById(id)
            .orElseThrow(() ->
                BusinessRuleViolationException.of("orderExecutionNotFound", "orderExecution", "No execution with id " + id)
            );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderExecutionDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all OrderExecutions");
        return orderExecutionRepository.findAll(pageable).map(orderExecutionMapper::toDto);
    }

    public Page<OrderExecutionDTO> findAllWithEagerRelationships(Pageable pageable) {
        return orderExecutionRepository.findAllWithEagerRelationships(pageable).map(orderExecutionMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OrderExecutionDTO> findOne(Long id) {
        LOG.debug("Request to get OrderExecution : {}", id);
        return orderExecutionRepository.findOneWithEagerRelationships(id).map(orderExecutionMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete OrderExecution : {}", id);
        orderExecutionRepository.deleteById(id);
    }
}
