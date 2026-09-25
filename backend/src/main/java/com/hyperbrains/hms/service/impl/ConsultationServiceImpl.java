package com.hyperbrains.hms.service.impl;

import com.hyperbrains.hms.domain.Consultation;
import com.hyperbrains.hms.repository.ConsultationRepository;
import com.hyperbrains.hms.service.ConsultationService;
import com.hyperbrains.hms.service.dto.ConsultationDTO;
import com.hyperbrains.hms.service.mapper.ConsultationMapper;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.hyperbrains.hms.domain.Consultation}.
 */
@Service
@Transactional
public class ConsultationServiceImpl implements ConsultationService {

    private static final Logger LOG = LoggerFactory.getLogger(ConsultationServiceImpl.class);

    private final ConsultationRepository consultationRepository;

    private final ConsultationMapper consultationMapper;

    public ConsultationServiceImpl(ConsultationRepository consultationRepository, ConsultationMapper consultationMapper) {
        this.consultationRepository = consultationRepository;
        this.consultationMapper = consultationMapper;
    }

    @Override
    public ConsultationDTO save(ConsultationDTO consultationDTO) {
        LOG.debug("Request to save Consultation : {}", consultationDTO);
        Consultation consultation = consultationMapper.toEntity(consultationDTO);
        consultation = consultationRepository.save(consultation);
        return consultationMapper.toDto(consultation);
    }

    @Override
    public ConsultationDTO update(ConsultationDTO consultationDTO) {
        LOG.debug("Request to update Consultation : {}", consultationDTO);
        Consultation consultation = consultationMapper.toEntity(consultationDTO);
        consultation = consultationRepository.save(consultation);
        return consultationMapper.toDto(consultation);
    }

    @Override
    public Optional<ConsultationDTO> partialUpdate(ConsultationDTO consultationDTO) {
        LOG.debug("Request to partially update Consultation : {}", consultationDTO);

        return consultationRepository
            .findById(consultationDTO.getId())
            .map(existingConsultation -> {
                consultationMapper.partialUpdate(existingConsultation, consultationDTO);

                return existingConsultation;
            })
            .map(consultationRepository::save)
            .map(consultationMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsultationDTO> findAll() {
        LOG.debug("Request to get all Consultations");
        return consultationRepository.findAll().stream().map(consultationMapper::toDto).collect(Collectors.toCollection(LinkedList::new));
    }

    public Page<ConsultationDTO> findAllWithEagerRelationships(Pageable pageable) {
        return consultationRepository.findAllWithEagerRelationships(pageable).map(consultationMapper::toDto);
    }

    /**
     *  Get all the consultations where Visit is {@code null}.
     *  @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<ConsultationDTO> findAllWhereVisitIsNull() {
        LOG.debug("Request to get all consultations where Visit is null");
        return StreamSupport.stream(consultationRepository.findAll().spliterator(), false)
            .filter(consultation -> consultation.getVisit() == null)
            .map(consultationMapper::toDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ConsultationDTO> findOne(Long id) {
        LOG.debug("Request to get Consultation : {}", id);
        return consultationRepository.findOneWithEagerRelationships(id).map(consultationMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Consultation : {}", id);
        consultationRepository.deleteById(id);
    }
}
