package com.hyperbrains.hms.service.impl;

import com.hyperbrains.hms.domain.OrderExecution;
import com.hyperbrains.hms.repository.OrderExecutionRepository;
import com.hyperbrains.hms.service.OrderExecutionService;
import com.hyperbrains.hms.service.dto.OrderExecutionDTO;
import com.hyperbrains.hms.service.mapper.OrderExecutionMapper;
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
                orderExecutionMapper.partialUpdate(existingOrderExecution, orderExecutionDTO);

                return existingOrderExecution;
            })
            .map(orderExecutionRepository::save)
            .map(orderExecutionMapper::toDto);
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
