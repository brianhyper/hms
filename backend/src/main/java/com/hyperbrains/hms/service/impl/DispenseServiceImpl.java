package com.hyperbrains.hms.service.impl;

import com.hyperbrains.hms.domain.Dispense;
import com.hyperbrains.hms.repository.DispenseRepository;
import com.hyperbrains.hms.service.DispenseService;
import com.hyperbrains.hms.service.dto.DispenseDTO;
import com.hyperbrains.hms.service.mapper.DispenseMapper;
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
                dispenseMapper.partialUpdate(existingDispense, dispenseDTO);

                return existingDispense;
            })
            .map(dispenseRepository::save)
            .map(dispenseMapper::toDto);
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
