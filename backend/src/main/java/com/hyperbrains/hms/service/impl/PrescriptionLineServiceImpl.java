package com.hyperbrains.hms.service.impl;

import com.hyperbrains.hms.domain.PrescriptionLine;
import com.hyperbrains.hms.repository.PrescriptionLineRepository;
import com.hyperbrains.hms.service.PrescriptionLineService;
import com.hyperbrains.hms.service.dto.PrescriptionLineDTO;
import com.hyperbrains.hms.service.mapper.PrescriptionLineMapper;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.hyperbrains.hms.domain.PrescriptionLine}.
 */
@Service
@Transactional
public class PrescriptionLineServiceImpl implements PrescriptionLineService {

    private static final Logger LOG = LoggerFactory.getLogger(PrescriptionLineServiceImpl.class);

    private final PrescriptionLineRepository prescriptionLineRepository;

    private final PrescriptionLineMapper prescriptionLineMapper;

    public PrescriptionLineServiceImpl(
        PrescriptionLineRepository prescriptionLineRepository,
        PrescriptionLineMapper prescriptionLineMapper
    ) {
        this.prescriptionLineRepository = prescriptionLineRepository;
        this.prescriptionLineMapper = prescriptionLineMapper;
    }

    @Override
    public PrescriptionLineDTO save(PrescriptionLineDTO prescriptionLineDTO) {
        LOG.debug("Request to save PrescriptionLine : {}", prescriptionLineDTO);
        PrescriptionLine prescriptionLine = prescriptionLineMapper.toEntity(prescriptionLineDTO);
        prescriptionLine = prescriptionLineRepository.save(prescriptionLine);
        return prescriptionLineMapper.toDto(prescriptionLine);
    }

    @Override
    public PrescriptionLineDTO update(PrescriptionLineDTO prescriptionLineDTO) {
        LOG.debug("Request to update PrescriptionLine : {}", prescriptionLineDTO);
        PrescriptionLine prescriptionLine = prescriptionLineMapper.toEntity(prescriptionLineDTO);
        prescriptionLine = prescriptionLineRepository.save(prescriptionLine);
        return prescriptionLineMapper.toDto(prescriptionLine);
    }

    @Override
    public Optional<PrescriptionLineDTO> partialUpdate(PrescriptionLineDTO prescriptionLineDTO) {
        LOG.debug("Request to partially update PrescriptionLine : {}", prescriptionLineDTO);

        return prescriptionLineRepository
            .findById(prescriptionLineDTO.getId())
            .map(existingPrescriptionLine -> {
                prescriptionLineMapper.partialUpdate(existingPrescriptionLine, prescriptionLineDTO);

                return existingPrescriptionLine;
            })
            .map(prescriptionLineRepository::save)
            .map(prescriptionLineMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionLineDTO> findAll() {
        LOG.debug("Request to get all PrescriptionLines");
        return prescriptionLineRepository
            .findAll()
            .stream()
            .map(prescriptionLineMapper::toDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PrescriptionLineDTO> findOne(Long id) {
        LOG.debug("Request to get PrescriptionLine : {}", id);
        return prescriptionLineRepository.findById(id).map(prescriptionLineMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete PrescriptionLine : {}", id);
        prescriptionLineRepository.deleteById(id);
    }
}
