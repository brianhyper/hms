package com.hyperbrains.hms.service.impl;

import com.hyperbrains.hms.domain.Admission;
import com.hyperbrains.hms.repository.AdmissionRepository;
import com.hyperbrains.hms.service.AdmissionService;
import com.hyperbrains.hms.service.dto.AdmissionDTO;
import com.hyperbrains.hms.service.mapper.AdmissionMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.hyperbrains.hms.domain.Admission}.
 */
@Service
@Transactional
public class AdmissionServiceImpl implements AdmissionService {

    private static final Logger LOG = LoggerFactory.getLogger(AdmissionServiceImpl.class);

    private final AdmissionRepository admissionRepository;

    private final AdmissionMapper admissionMapper;

    public AdmissionServiceImpl(AdmissionRepository admissionRepository, AdmissionMapper admissionMapper) {
        this.admissionRepository = admissionRepository;
        this.admissionMapper = admissionMapper;
    }

    @Override
    public AdmissionDTO save(AdmissionDTO admissionDTO) {
        LOG.debug("Request to save Admission : {}", admissionDTO);
        Admission admission = admissionMapper.toEntity(admissionDTO);
        admission = admissionRepository.save(admission);
        return admissionMapper.toDto(admission);
    }

    @Override
    public AdmissionDTO update(AdmissionDTO admissionDTO) {
        LOG.debug("Request to update Admission : {}", admissionDTO);
        Admission admission = admissionMapper.toEntity(admissionDTO);
        admission = admissionRepository.save(admission);
        return admissionMapper.toDto(admission);
    }

    @Override
    public Optional<AdmissionDTO> partialUpdate(AdmissionDTO admissionDTO) {
        LOG.debug("Request to partially update Admission : {}", admissionDTO);

        return admissionRepository
            .findById(admissionDTO.getId())
            .map(existingAdmission -> {
                admissionMapper.partialUpdate(existingAdmission, admissionDTO);

                return existingAdmission;
            })
            .map(admissionRepository::save)
            .map(admissionMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdmissionDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Admissions");
        return admissionRepository.findAll(pageable).map(admissionMapper::toDto);
    }

    public Page<AdmissionDTO> findAllWithEagerRelationships(Pageable pageable) {
        return admissionRepository.findAllWithEagerRelationships(pageable).map(admissionMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AdmissionDTO> findOne(Long id) {
        LOG.debug("Request to get Admission : {}", id);
        return admissionRepository.findOneWithEagerRelationships(id).map(admissionMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Admission : {}", id);
        admissionRepository.deleteById(id);
    }
}
