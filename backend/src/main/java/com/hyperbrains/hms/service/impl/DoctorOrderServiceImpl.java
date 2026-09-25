package com.hyperbrains.hms.service.impl;

import com.hyperbrains.hms.domain.DoctorOrder;
import com.hyperbrains.hms.repository.DoctorOrderRepository;
import com.hyperbrains.hms.service.DoctorOrderService;
import com.hyperbrains.hms.service.dto.DoctorOrderDTO;
import com.hyperbrains.hms.service.mapper.DoctorOrderMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.hyperbrains.hms.domain.DoctorOrder}.
 */
@Service
@Transactional
public class DoctorOrderServiceImpl implements DoctorOrderService {

    private static final Logger LOG = LoggerFactory.getLogger(DoctorOrderServiceImpl.class);

    private final DoctorOrderRepository doctorOrderRepository;

    private final DoctorOrderMapper doctorOrderMapper;

    public DoctorOrderServiceImpl(DoctorOrderRepository doctorOrderRepository, DoctorOrderMapper doctorOrderMapper) {
        this.doctorOrderRepository = doctorOrderRepository;
        this.doctorOrderMapper = doctorOrderMapper;
    }

    @Override
    public DoctorOrderDTO save(DoctorOrderDTO doctorOrderDTO) {
        LOG.debug("Request to save DoctorOrder : {}", doctorOrderDTO);
        DoctorOrder doctorOrder = doctorOrderMapper.toEntity(doctorOrderDTO);
        doctorOrder = doctorOrderRepository.save(doctorOrder);
        return doctorOrderMapper.toDto(doctorOrder);
    }

    @Override
    public DoctorOrderDTO update(DoctorOrderDTO doctorOrderDTO) {
        LOG.debug("Request to update DoctorOrder : {}", doctorOrderDTO);
        DoctorOrder doctorOrder = doctorOrderMapper.toEntity(doctorOrderDTO);
        doctorOrder = doctorOrderRepository.save(doctorOrder);
        return doctorOrderMapper.toDto(doctorOrder);
    }

    @Override
    public Optional<DoctorOrderDTO> partialUpdate(DoctorOrderDTO doctorOrderDTO) {
        LOG.debug("Request to partially update DoctorOrder : {}", doctorOrderDTO);

        return doctorOrderRepository
            .findById(doctorOrderDTO.getId())
            .map(existingDoctorOrder -> {
                doctorOrderMapper.partialUpdate(existingDoctorOrder, doctorOrderDTO);

                return existingDoctorOrder;
            })
            .map(doctorOrderRepository::save)
            .map(doctorOrderMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DoctorOrderDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all DoctorOrders");
        return doctorOrderRepository.findAll(pageable).map(doctorOrderMapper::toDto);
    }

    public Page<DoctorOrderDTO> findAllWithEagerRelationships(Pageable pageable) {
        return doctorOrderRepository.findAllWithEagerRelationships(pageable).map(doctorOrderMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DoctorOrderDTO> findOne(Long id) {
        LOG.debug("Request to get DoctorOrder : {}", id);
        return doctorOrderRepository.findOneWithEagerRelationships(id).map(doctorOrderMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete DoctorOrder : {}", id);
        doctorOrderRepository.deleteById(id);
    }
}
