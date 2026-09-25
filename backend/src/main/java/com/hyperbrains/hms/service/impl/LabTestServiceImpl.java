package com.hyperbrains.hms.service.impl;

import com.hyperbrains.hms.domain.LabTest;
import com.hyperbrains.hms.repository.LabTestRepository;
import com.hyperbrains.hms.service.LabTestService;
import com.hyperbrains.hms.service.dto.LabTestDTO;
import com.hyperbrains.hms.service.mapper.LabTestMapper;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.hyperbrains.hms.domain.LabTest}.
 */
@Service
@Transactional
public class LabTestServiceImpl implements LabTestService {

    private static final Logger LOG = LoggerFactory.getLogger(LabTestServiceImpl.class);

    private final LabTestRepository labTestRepository;

    private final LabTestMapper labTestMapper;

    public LabTestServiceImpl(LabTestRepository labTestRepository, LabTestMapper labTestMapper) {
        this.labTestRepository = labTestRepository;
        this.labTestMapper = labTestMapper;
    }

    @Override
    public LabTestDTO save(LabTestDTO labTestDTO) {
        LOG.debug("Request to save LabTest : {}", labTestDTO);
        LabTest labTest = labTestMapper.toEntity(labTestDTO);
        labTest = labTestRepository.save(labTest);
        return labTestMapper.toDto(labTest);
    }

    @Override
    public LabTestDTO update(LabTestDTO labTestDTO) {
        LOG.debug("Request to update LabTest : {}", labTestDTO);
        LabTest labTest = labTestMapper.toEntity(labTestDTO);
        labTest = labTestRepository.save(labTest);
        return labTestMapper.toDto(labTest);
    }

    @Override
    public Optional<LabTestDTO> partialUpdate(LabTestDTO labTestDTO) {
        LOG.debug("Request to partially update LabTest : {}", labTestDTO);

        return labTestRepository
            .findById(labTestDTO.getId())
            .map(existingLabTest -> {
                labTestMapper.partialUpdate(existingLabTest, labTestDTO);

                return existingLabTest;
            })
            .map(labTestRepository::save)
            .map(labTestMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LabTestDTO> findAll() {
        LOG.debug("Request to get all LabTests");
        return labTestRepository.findAll().stream().map(labTestMapper::toDto).collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<LabTestDTO> findOne(Long id) {
        LOG.debug("Request to get LabTest : {}", id);
        return labTestRepository.findById(id).map(labTestMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete LabTest : {}", id);
        labTestRepository.deleteById(id);
    }
}
