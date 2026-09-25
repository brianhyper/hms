package com.hyperbrains.hms.service.impl;

import com.hyperbrains.hms.domain.Prescription;
import com.hyperbrains.hms.repository.PrescriptionRepository;
import com.hyperbrains.hms.service.PrescriptionService;
import com.hyperbrains.hms.service.dto.PrescriptionDTO;
import com.hyperbrains.hms.service.mapper.PrescriptionMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.hyperbrains.hms.domain.Prescription}.
 */
@Service
@Transactional
public class PrescriptionServiceImpl implements PrescriptionService {

    private static final Logger LOG = LoggerFactory.getLogger(PrescriptionServiceImpl.class);

    private final PrescriptionRepository prescriptionRepository;

    private final PrescriptionMapper prescriptionMapper;

    public PrescriptionServiceImpl(PrescriptionRepository prescriptionRepository, PrescriptionMapper prescriptionMapper) {
        this.prescriptionRepository = prescriptionRepository;
        this.prescriptionMapper = prescriptionMapper;
    }

    @Override
    public PrescriptionDTO save(PrescriptionDTO prescriptionDTO) {
        LOG.debug("Request to save Prescription : {}", prescriptionDTO);
        Prescription prescription = prescriptionMapper.toEntity(prescriptionDTO);
        prescription = prescriptionRepository.save(prescription);
        return prescriptionMapper.toDto(prescription);
    }

    @Override
    public PrescriptionDTO update(PrescriptionDTO prescriptionDTO) {
        LOG.debug("Request to update Prescription : {}", prescriptionDTO);
        Prescription prescription = prescriptionMapper.toEntity(prescriptionDTO);
        prescription = prescriptionRepository.save(prescription);
        return prescriptionMapper.toDto(prescription);
    }

    @Override
    public Optional<PrescriptionDTO> partialUpdate(PrescriptionDTO prescriptionDTO) {
        LOG.debug("Request to partially update Prescription : {}", prescriptionDTO);

        return prescriptionRepository
            .findById(prescriptionDTO.getId())
            .map(existingPrescription -> {
                prescriptionMapper.partialUpdate(existingPrescription, prescriptionDTO);

                return existingPrescription;
            })
            .map(prescriptionRepository::save)
            .map(prescriptionMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PrescriptionDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Prescriptions");
        return prescriptionRepository.findAll(pageable).map(prescriptionMapper::toDto);
    }

    public Page<PrescriptionDTO> findAllWithEagerRelationships(Pageable pageable) {
        return prescriptionRepository.findAllWithEagerRelationships(pageable).map(prescriptionMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PrescriptionDTO> findOne(Long id) {
        LOG.debug("Request to get Prescription : {}", id);
        return prescriptionRepository.findOneWithEagerRelationships(id).map(prescriptionMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Prescription : {}", id);
        prescriptionRepository.deleteById(id);
    }
}
