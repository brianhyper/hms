package com.hyperbrains.hms.service.impl;

import com.hyperbrains.hms.domain.DiagnosticOrder;
import com.hyperbrains.hms.repository.DiagnosticOrderRepository;
import com.hyperbrains.hms.service.DiagnosticOrderService;
import com.hyperbrains.hms.service.dto.DiagnosticOrderDTO;
import com.hyperbrains.hms.service.mapper.DiagnosticOrderMapper;
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
                diagnosticOrderMapper.partialUpdate(existingDiagnosticOrder, diagnosticOrderDTO);

                return existingDiagnosticOrder;
            })
            .map(diagnosticOrderRepository::save)
            .map(diagnosticOrderMapper::toDto);
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
