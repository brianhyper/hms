package com.hyperbrains.hms.service.impl;

import com.hyperbrains.hms.domain.BedType;
import com.hyperbrains.hms.repository.BedTypeRepository;
import com.hyperbrains.hms.service.BedTypeService;
import com.hyperbrains.hms.service.dto.BedTypeDTO;
import com.hyperbrains.hms.service.mapper.BedTypeMapper;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.hyperbrains.hms.domain.BedType}.
 */
@Service
@Transactional
public class BedTypeServiceImpl implements BedTypeService {

    private static final Logger LOG = LoggerFactory.getLogger(BedTypeServiceImpl.class);

    private final BedTypeRepository bedTypeRepository;

    private final BedTypeMapper bedTypeMapper;

    public BedTypeServiceImpl(BedTypeRepository bedTypeRepository, BedTypeMapper bedTypeMapper) {
        this.bedTypeRepository = bedTypeRepository;
        this.bedTypeMapper = bedTypeMapper;
    }

    @Override
    public BedTypeDTO save(BedTypeDTO bedTypeDTO) {
        LOG.debug("Request to save BedType : {}", bedTypeDTO);
        BedType bedType = bedTypeMapper.toEntity(bedTypeDTO);
        bedType = bedTypeRepository.save(bedType);
        return bedTypeMapper.toDto(bedType);
    }

    @Override
    public BedTypeDTO update(BedTypeDTO bedTypeDTO) {
        LOG.debug("Request to update BedType : {}", bedTypeDTO);
        BedType bedType = bedTypeMapper.toEntity(bedTypeDTO);
        bedType = bedTypeRepository.save(bedType);
        return bedTypeMapper.toDto(bedType);
    }

    @Override
    public Optional<BedTypeDTO> partialUpdate(BedTypeDTO bedTypeDTO) {
        LOG.debug("Request to partially update BedType : {}", bedTypeDTO);

        return bedTypeRepository
            .findById(bedTypeDTO.getId())
            .map(existingBedType -> {
                bedTypeMapper.partialUpdate(existingBedType, bedTypeDTO);

                return existingBedType;
            })
            .map(bedTypeRepository::save)
            .map(bedTypeMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BedTypeDTO> findAll() {
        LOG.debug("Request to get all BedTypes");
        return bedTypeRepository.findAll().stream().map(bedTypeMapper::toDto).collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BedTypeDTO> findOne(Long id) {
        LOG.debug("Request to get BedType : {}", id);
        return bedTypeRepository.findById(id).map(bedTypeMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete BedType : {}", id);
        bedTypeRepository.deleteById(id);
    }
}
