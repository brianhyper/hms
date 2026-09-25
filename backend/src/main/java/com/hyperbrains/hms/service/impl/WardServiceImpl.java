package com.hyperbrains.hms.service.impl;

import com.hyperbrains.hms.domain.Ward;
import com.hyperbrains.hms.repository.WardRepository;
import com.hyperbrains.hms.service.WardService;
import com.hyperbrains.hms.service.dto.WardDTO;
import com.hyperbrains.hms.service.mapper.WardMapper;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.hyperbrains.hms.domain.Ward}.
 */
@Service
@Transactional
public class WardServiceImpl implements WardService {

    private static final Logger LOG = LoggerFactory.getLogger(WardServiceImpl.class);

    private final WardRepository wardRepository;

    private final WardMapper wardMapper;

    public WardServiceImpl(WardRepository wardRepository, WardMapper wardMapper) {
        this.wardRepository = wardRepository;
        this.wardMapper = wardMapper;
    }

    @Override
    public WardDTO save(WardDTO wardDTO) {
        LOG.debug("Request to save Ward : {}", wardDTO);
        Ward ward = wardMapper.toEntity(wardDTO);
        ward = wardRepository.save(ward);
        return wardMapper.toDto(ward);
    }

    @Override
    public WardDTO update(WardDTO wardDTO) {
        LOG.debug("Request to update Ward : {}", wardDTO);
        Ward ward = wardMapper.toEntity(wardDTO);
        ward = wardRepository.save(ward);
        return wardMapper.toDto(ward);
    }

    @Override
    public Optional<WardDTO> partialUpdate(WardDTO wardDTO) {
        LOG.debug("Request to partially update Ward : {}", wardDTO);

        return wardRepository
            .findById(wardDTO.getId())
            .map(existingWard -> {
                wardMapper.partialUpdate(existingWard, wardDTO);

                return existingWard;
            })
            .map(wardRepository::save)
            .map(wardMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WardDTO> findAll() {
        LOG.debug("Request to get all Wards");
        return wardRepository.findAll().stream().map(wardMapper::toDto).collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<WardDTO> findOne(Long id) {
        LOG.debug("Request to get Ward : {}", id);
        return wardRepository.findById(id).map(wardMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Ward : {}", id);
        wardRepository.deleteById(id);
    }
}
