package com.hyperbrains.hms.service.impl;

import com.hyperbrains.hms.domain.Patient;
import com.hyperbrains.hms.repository.PatientRepository;
import com.hyperbrains.hms.service.PatientService;
import com.hyperbrains.hms.service.dto.PatientDTO;
import com.hyperbrains.hms.service.mapper.PatientMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.hyperbrains.hms.domain.Patient}.
 */
@Service
@Transactional
public class PatientServiceImpl implements PatientService {

    private static final Logger LOG = LoggerFactory.getLogger(PatientServiceImpl.class);

    private final PatientRepository patientRepository;

    private final PatientMapper patientMapper;

    public PatientServiceImpl(PatientRepository patientRepository, PatientMapper patientMapper) {
        this.patientRepository = patientRepository;
        this.patientMapper = patientMapper;
    }

    @Override
    public PatientDTO save(PatientDTO patientDTO) {
        LOG.debug("Request to save Patient : {}", patientDTO);
        Patient patient = patientMapper.toEntity(patientDTO);
        patient = patientRepository.save(patient);
        return patientMapper.toDto(patient);
    }

    @Override
    public PatientDTO update(PatientDTO patientDTO) {
        LOG.debug("Request to update Patient : {}", patientDTO);
        Patient patient = patientMapper.toEntity(patientDTO);
        patient = patientRepository.save(patient);
        return patientMapper.toDto(patient);
    }

    @Override
    public Optional<PatientDTO> partialUpdate(PatientDTO patientDTO) {
        LOG.debug("Request to partially update Patient : {}", patientDTO);

        return patientRepository
            .findById(patientDTO.getId())
            .map(existingPatient -> {
                patientMapper.partialUpdate(existingPatient, patientDTO);

                return existingPatient;
            })
            .map(patientRepository::save)
            .map(patientMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PatientDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Patients");
        return patientRepository.findAll(pageable).map(patientMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PatientDTO> findOne(Long id) {
        LOG.debug("Request to get Patient : {}", id);
        return patientRepository.findById(id).map(patientMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Patient : {}", id);
        patientRepository.deleteById(id);
    }
}
