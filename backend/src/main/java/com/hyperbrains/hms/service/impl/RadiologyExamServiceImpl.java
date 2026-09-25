package com.hyperbrains.hms.service.impl;

import com.hyperbrains.hms.domain.RadiologyExam;
import com.hyperbrains.hms.repository.RadiologyExamRepository;
import com.hyperbrains.hms.service.RadiologyExamService;
import com.hyperbrains.hms.service.dto.RadiologyExamDTO;
import com.hyperbrains.hms.service.mapper.RadiologyExamMapper;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.hyperbrains.hms.domain.RadiologyExam}.
 */
@Service
@Transactional
public class RadiologyExamServiceImpl implements RadiologyExamService {

    private static final Logger LOG = LoggerFactory.getLogger(RadiologyExamServiceImpl.class);

    private final RadiologyExamRepository radiologyExamRepository;

    private final RadiologyExamMapper radiologyExamMapper;

    public RadiologyExamServiceImpl(RadiologyExamRepository radiologyExamRepository, RadiologyExamMapper radiologyExamMapper) {
        this.radiologyExamRepository = radiologyExamRepository;
        this.radiologyExamMapper = radiologyExamMapper;
    }

    @Override
    public RadiologyExamDTO save(RadiologyExamDTO radiologyExamDTO) {
        LOG.debug("Request to save RadiologyExam : {}", radiologyExamDTO);
        RadiologyExam radiologyExam = radiologyExamMapper.toEntity(radiologyExamDTO);
        radiologyExam = radiologyExamRepository.save(radiologyExam);
        return radiologyExamMapper.toDto(radiologyExam);
    }

    @Override
    public RadiologyExamDTO update(RadiologyExamDTO radiologyExamDTO) {
        LOG.debug("Request to update RadiologyExam : {}", radiologyExamDTO);
        RadiologyExam radiologyExam = radiologyExamMapper.toEntity(radiologyExamDTO);
        radiologyExam = radiologyExamRepository.save(radiologyExam);
        return radiologyExamMapper.toDto(radiologyExam);
    }

    @Override
    public Optional<RadiologyExamDTO> partialUpdate(RadiologyExamDTO radiologyExamDTO) {
        LOG.debug("Request to partially update RadiologyExam : {}", radiologyExamDTO);

        return radiologyExamRepository
            .findById(radiologyExamDTO.getId())
            .map(existingRadiologyExam -> {
                radiologyExamMapper.partialUpdate(existingRadiologyExam, radiologyExamDTO);

                return existingRadiologyExam;
            })
            .map(radiologyExamRepository::save)
            .map(radiologyExamMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RadiologyExamDTO> findAll() {
        LOG.debug("Request to get all RadiologyExams");
        return radiologyExamRepository.findAll().stream().map(radiologyExamMapper::toDto).collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RadiologyExamDTO> findOne(Long id) {
        LOG.debug("Request to get RadiologyExam : {}", id);
        return radiologyExamRepository.findById(id).map(radiologyExamMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete RadiologyExam : {}", id);
        radiologyExamRepository.deleteById(id);
    }
}
