package com.hyperbrains.hms.service.impl;

import com.hyperbrains.hms.domain.WardCover;
import com.hyperbrains.hms.repository.WardCoverRepository;
import com.hyperbrains.hms.service.WardCoverService;
import com.hyperbrains.hms.service.dto.WardCoverDTO;
import com.hyperbrains.hms.service.mapper.WardCoverMapper;
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
 * Service Implementation for managing {@link com.hyperbrains.hms.domain.WardCover}.
 */
@Service
@Transactional
public class WardCoverServiceImpl implements WardCoverService {

    private static final Logger LOG = LoggerFactory.getLogger(WardCoverServiceImpl.class);

    private final WardCoverRepository wardCoverRepository;

    private final WardCoverMapper wardCoverMapper;

    public WardCoverServiceImpl(WardCoverRepository wardCoverRepository, WardCoverMapper wardCoverMapper) {
        this.wardCoverRepository = wardCoverRepository;
        this.wardCoverMapper = wardCoverMapper;
    }

    @Override
    public WardCoverDTO save(WardCoverDTO wardCoverDTO) {
        LOG.debug("Request to save WardCover : {}", wardCoverDTO);
        WardCover wardCover = wardCoverMapper.toEntity(wardCoverDTO);
        wardCover = wardCoverRepository.save(wardCover);
        return wardCoverMapper.toDto(wardCover);
    }

    @Override
    public WardCoverDTO update(WardCoverDTO wardCoverDTO) {
        LOG.debug("Request to update WardCover : {}", wardCoverDTO);
        WardCover wardCover = wardCoverMapper.toEntity(wardCoverDTO);
        wardCover = wardCoverRepository.save(wardCover);
        return wardCoverMapper.toDto(wardCover);
    }

    @Override
    public Optional<WardCoverDTO> partialUpdate(WardCoverDTO wardCoverDTO) {
        LOG.debug("Request to partially update WardCover : {}", wardCoverDTO);

        return wardCoverRepository
            .findById(wardCoverDTO.getId())
            .map(existingWardCover -> {
                wardCoverMapper.partialUpdate(existingWardCover, wardCoverDTO);

                return existingWardCover;
            })
            .map(wardCoverRepository::save)
            .map(wardCoverMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WardCoverDTO> findAll() {
        LOG.debug("Request to get all WardCovers");
        return wardCoverRepository.findAll().stream().map(wardCoverMapper::toDto).collect(Collectors.toCollection(LinkedList::new));
    }

    public Page<WardCoverDTO> findAllWithEagerRelationships(Pageable pageable) {
        return wardCoverRepository.findAllWithEagerRelationships(pageable).map(wardCoverMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<WardCoverDTO> findOne(Long id) {
        LOG.debug("Request to get WardCover : {}", id);
        return wardCoverRepository.findOneWithEagerRelationships(id).map(wardCoverMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete WardCover : {}", id);
        wardCoverRepository.deleteById(id);
    }
}
