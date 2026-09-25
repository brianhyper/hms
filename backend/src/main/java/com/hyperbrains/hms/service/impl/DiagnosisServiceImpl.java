package com.hyperbrains.hms.service.impl;

import com.hyperbrains.hms.domain.Diagnosis;
import com.hyperbrains.hms.repository.DiagnosisRepository;
import com.hyperbrains.hms.service.DiagnosisService;
import com.hyperbrains.hms.service.dto.DiagnosisDTO;
import com.hyperbrains.hms.service.mapper.DiagnosisMapper;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.hyperbrains.hms.domain.Diagnosis}.
 */
@Service
@Transactional
public class DiagnosisServiceImpl implements DiagnosisService {

    private static final Logger LOG = LoggerFactory.getLogger(DiagnosisServiceImpl.class);

    private final DiagnosisRepository diagnosisRepository;

    private final DiagnosisMapper diagnosisMapper;

    public DiagnosisServiceImpl(DiagnosisRepository diagnosisRepository, DiagnosisMapper diagnosisMapper) {
        this.diagnosisRepository = diagnosisRepository;
        this.diagnosisMapper = diagnosisMapper;
    }

    @Override
    public DiagnosisDTO save(DiagnosisDTO diagnosisDTO) {
        LOG.debug("Request to save Diagnosis : {}", diagnosisDTO);
        Diagnosis diagnosis = diagnosisMapper.toEntity(diagnosisDTO);
        diagnosis = diagnosisRepository.save(diagnosis);
        return diagnosisMapper.toDto(diagnosis);
    }

    @Override
    public DiagnosisDTO update(DiagnosisDTO diagnosisDTO) {
        LOG.debug("Request to update Diagnosis : {}", diagnosisDTO);
        Diagnosis diagnosis = diagnosisMapper.toEntity(diagnosisDTO);
        diagnosis = diagnosisRepository.save(diagnosis);
        return diagnosisMapper.toDto(diagnosis);
    }

    @Override
    public Optional<DiagnosisDTO> partialUpdate(DiagnosisDTO diagnosisDTO) {
        LOG.debug("Request to partially update Diagnosis : {}", diagnosisDTO);

        return diagnosisRepository
            .findById(diagnosisDTO.getId())
            .map(existingDiagnosis -> {
                diagnosisMapper.partialUpdate(existingDiagnosis, diagnosisDTO);

                return existingDiagnosis;
            })
            .map(diagnosisRepository::save)
            .map(diagnosisMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DiagnosisDTO> findAll() {
        LOG.debug("Request to get all Diagnoses");
        return diagnosisRepository.findAll().stream().map(diagnosisMapper::toDto).collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DiagnosisDTO> findOne(Long id) {
        LOG.debug("Request to get Diagnosis : {}", id);
        return diagnosisRepository.findById(id).map(diagnosisMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Diagnosis : {}", id);
        diagnosisRepository.deleteById(id);
    }
}
