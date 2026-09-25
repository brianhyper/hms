package com.hyperbrains.hms.service.impl;

import com.hyperbrains.hms.domain.VitalSigns;
import com.hyperbrains.hms.repository.VitalSignsRepository;
import com.hyperbrains.hms.service.VitalSignsService;
import com.hyperbrains.hms.service.dto.VitalSignsDTO;
import com.hyperbrains.hms.service.mapper.VitalSignsMapper;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.hyperbrains.hms.domain.VitalSigns}.
 */
@Service
@Transactional
public class VitalSignsServiceImpl implements VitalSignsService {

    private static final Logger LOG = LoggerFactory.getLogger(VitalSignsServiceImpl.class);

    private final VitalSignsRepository vitalSignsRepository;

    private final VitalSignsMapper vitalSignsMapper;

    public VitalSignsServiceImpl(VitalSignsRepository vitalSignsRepository, VitalSignsMapper vitalSignsMapper) {
        this.vitalSignsRepository = vitalSignsRepository;
        this.vitalSignsMapper = vitalSignsMapper;
    }

    @Override
    public VitalSignsDTO save(VitalSignsDTO vitalSignsDTO) {
        LOG.debug("Request to save VitalSigns : {}", vitalSignsDTO);
        VitalSigns vitalSigns = vitalSignsMapper.toEntity(vitalSignsDTO);
        vitalSigns = vitalSignsRepository.save(vitalSigns);
        return vitalSignsMapper.toDto(vitalSigns);
    }

    @Override
    public VitalSignsDTO update(VitalSignsDTO vitalSignsDTO) {
        LOG.debug("Request to update VitalSigns : {}", vitalSignsDTO);
        VitalSigns vitalSigns = vitalSignsMapper.toEntity(vitalSignsDTO);
        vitalSigns = vitalSignsRepository.save(vitalSigns);
        return vitalSignsMapper.toDto(vitalSigns);
    }

    @Override
    public Optional<VitalSignsDTO> partialUpdate(VitalSignsDTO vitalSignsDTO) {
        LOG.debug("Request to partially update VitalSigns : {}", vitalSignsDTO);

        return vitalSignsRepository
            .findById(vitalSignsDTO.getId())
            .map(existingVitalSigns -> {
                vitalSignsMapper.partialUpdate(existingVitalSigns, vitalSignsDTO);

                return existingVitalSigns;
            })
            .map(vitalSignsRepository::save)
            .map(vitalSignsMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VitalSignsDTO> findAll() {
        LOG.debug("Request to get all VitalSignses");
        return vitalSignsRepository.findAll().stream().map(vitalSignsMapper::toDto).collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     *  Get all the vitalSignses where Visit is {@code null}.
     *  @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<VitalSignsDTO> findAllWhereVisitIsNull() {
        LOG.debug("Request to get all vitalSignses where Visit is null");
        return StreamSupport.stream(vitalSignsRepository.findAll().spliterator(), false)
            .filter(vitalSigns -> vitalSigns.getVisit() == null)
            .map(vitalSignsMapper::toDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<VitalSignsDTO> findOne(Long id) {
        LOG.debug("Request to get VitalSigns : {}", id);
        return vitalSignsRepository.findById(id).map(vitalSignsMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete VitalSigns : {}", id);
        vitalSignsRepository.deleteById(id);
    }
}
