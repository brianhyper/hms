package com.hyperbrains.hms.service.impl;

import com.hyperbrains.hms.domain.Visit;
import com.hyperbrains.hms.repository.VisitRepository;
import com.hyperbrains.hms.service.VisitService;
import com.hyperbrains.hms.service.dto.VisitDTO;
import com.hyperbrains.hms.service.mapper.VisitMapper;
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
 * Service Implementation for managing {@link com.hyperbrains.hms.domain.Visit}.
 */
@Service
@Transactional
public class VisitServiceImpl implements VisitService {

    private static final Logger LOG = LoggerFactory.getLogger(VisitServiceImpl.class);

    private final VisitRepository visitRepository;

    private final VisitMapper visitMapper;

    public VisitServiceImpl(VisitRepository visitRepository, VisitMapper visitMapper) {
        this.visitRepository = visitRepository;
        this.visitMapper = visitMapper;
    }

    @Override
    public VisitDTO save(VisitDTO visitDTO) {
        LOG.debug("Request to save Visit : {}", visitDTO);
        Visit visit = visitMapper.toEntity(visitDTO);
        visit = visitRepository.save(visit);
        return visitMapper.toDto(visit);
    }

    @Override
    public VisitDTO update(VisitDTO visitDTO) {
        LOG.debug("Request to update Visit : {}", visitDTO);
        Visit visit = visitMapper.toEntity(visitDTO);
        visit = visitRepository.save(visit);
        return visitMapper.toDto(visit);
    }

    @Override
    public Optional<VisitDTO> partialUpdate(VisitDTO visitDTO) {
        LOG.debug("Request to partially update Visit : {}", visitDTO);

        return visitRepository
            .findById(visitDTO.getId())
            .map(existingVisit -> {
                visitMapper.partialUpdate(existingVisit, visitDTO);

                return existingVisit;
            })
            .map(visitRepository::save)
            .map(visitMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VisitDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Visits");
        return visitRepository.findAll(pageable).map(visitMapper::toDto);
    }

    /**
     *  Get all the visits where Appointment is {@code null}.
     *  @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<VisitDTO> findAllWhereAppointmentIsNull() {
        LOG.debug("Request to get all visits where Appointment is null");
        return StreamSupport.stream(visitRepository.findAll().spliterator(), false)
            .filter(visit -> visit.getAppointment() == null)
            .map(visitMapper::toDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<VisitDTO> findOne(Long id) {
        LOG.debug("Request to get Visit : {}", id);
        return visitRepository.findById(id).map(visitMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Visit : {}", id);
        visitRepository.deleteById(id);
    }
}
