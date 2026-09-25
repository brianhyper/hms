package com.hyperbrains.hms.service.impl;

import com.hyperbrains.hms.domain.InpatientVitals;
import com.hyperbrains.hms.repository.InpatientVitalsRepository;
import com.hyperbrains.hms.service.InpatientVitalsService;
import com.hyperbrains.hms.service.dto.InpatientVitalsDTO;
import com.hyperbrains.hms.service.mapper.InpatientVitalsMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.hyperbrains.hms.domain.InpatientVitals}.
 */
@Service
@Transactional
public class InpatientVitalsServiceImpl implements InpatientVitalsService {

    private static final Logger LOG = LoggerFactory.getLogger(InpatientVitalsServiceImpl.class);

    private final InpatientVitalsRepository inpatientVitalsRepository;

    private final InpatientVitalsMapper inpatientVitalsMapper;

    public InpatientVitalsServiceImpl(InpatientVitalsRepository inpatientVitalsRepository, InpatientVitalsMapper inpatientVitalsMapper) {
        this.inpatientVitalsRepository = inpatientVitalsRepository;
        this.inpatientVitalsMapper = inpatientVitalsMapper;
    }

    @Override
    public InpatientVitalsDTO save(InpatientVitalsDTO inpatientVitalsDTO) {
        LOG.debug("Request to save InpatientVitals : {}", inpatientVitalsDTO);
        InpatientVitals inpatientVitals = inpatientVitalsMapper.toEntity(inpatientVitalsDTO);
        inpatientVitals = inpatientVitalsRepository.save(inpatientVitals);
        return inpatientVitalsMapper.toDto(inpatientVitals);
    }

    @Override
    public InpatientVitalsDTO update(InpatientVitalsDTO inpatientVitalsDTO) {
        LOG.debug("Request to update InpatientVitals : {}", inpatientVitalsDTO);
        InpatientVitals inpatientVitals = inpatientVitalsMapper.toEntity(inpatientVitalsDTO);
        inpatientVitals = inpatientVitalsRepository.save(inpatientVitals);
        return inpatientVitalsMapper.toDto(inpatientVitals);
    }

    @Override
    public Optional<InpatientVitalsDTO> partialUpdate(InpatientVitalsDTO inpatientVitalsDTO) {
        LOG.debug("Request to partially update InpatientVitals : {}", inpatientVitalsDTO);

        return inpatientVitalsRepository
            .findById(inpatientVitalsDTO.getId())
            .map(existingInpatientVitals -> {
                inpatientVitalsMapper.partialUpdate(existingInpatientVitals, inpatientVitalsDTO);

                return existingInpatientVitals;
            })
            .map(inpatientVitalsRepository::save)
            .map(inpatientVitalsMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InpatientVitalsDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all InpatientVitalses");
        return inpatientVitalsRepository.findAll(pageable).map(inpatientVitalsMapper::toDto);
    }

    public Page<InpatientVitalsDTO> findAllWithEagerRelationships(Pageable pageable) {
        return inpatientVitalsRepository.findAllWithEagerRelationships(pageable).map(inpatientVitalsMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<InpatientVitalsDTO> findOne(Long id) {
        LOG.debug("Request to get InpatientVitals : {}", id);
        return inpatientVitalsRepository.findOneWithEagerRelationships(id).map(inpatientVitalsMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete InpatientVitals : {}", id);
        inpatientVitalsRepository.deleteById(id);
    }
}
