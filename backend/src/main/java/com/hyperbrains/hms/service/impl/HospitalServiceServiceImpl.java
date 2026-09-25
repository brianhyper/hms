package com.hyperbrains.hms.service.impl;

import com.hyperbrains.hms.domain.HospitalService;
import com.hyperbrains.hms.repository.HospitalServiceRepository;
import com.hyperbrains.hms.service.HospitalServiceService;
import com.hyperbrains.hms.service.dto.HospitalServiceDTO;
import com.hyperbrains.hms.service.mapper.HospitalServiceMapper;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.hyperbrains.hms.domain.HospitalService}.
 */
@Service
@Transactional
public class HospitalServiceServiceImpl implements HospitalServiceService {

    private static final Logger LOG = LoggerFactory.getLogger(HospitalServiceServiceImpl.class);

    private final HospitalServiceRepository hospitalServiceRepository;

    private final HospitalServiceMapper hospitalServiceMapper;

    public HospitalServiceServiceImpl(HospitalServiceRepository hospitalServiceRepository, HospitalServiceMapper hospitalServiceMapper) {
        this.hospitalServiceRepository = hospitalServiceRepository;
        this.hospitalServiceMapper = hospitalServiceMapper;
    }

    @Override
    public HospitalServiceDTO save(HospitalServiceDTO hospitalServiceDTO) {
        LOG.debug("Request to save HospitalService : {}", hospitalServiceDTO);
        HospitalService hospitalService = hospitalServiceMapper.toEntity(hospitalServiceDTO);
        hospitalService = hospitalServiceRepository.save(hospitalService);
        return hospitalServiceMapper.toDto(hospitalService);
    }

    @Override
    public HospitalServiceDTO update(HospitalServiceDTO hospitalServiceDTO) {
        LOG.debug("Request to update HospitalService : {}", hospitalServiceDTO);
        HospitalService hospitalService = hospitalServiceMapper.toEntity(hospitalServiceDTO);
        hospitalService = hospitalServiceRepository.save(hospitalService);
        return hospitalServiceMapper.toDto(hospitalService);
    }

    @Override
    public Optional<HospitalServiceDTO> partialUpdate(HospitalServiceDTO hospitalServiceDTO) {
        LOG.debug("Request to partially update HospitalService : {}", hospitalServiceDTO);

        return hospitalServiceRepository
            .findById(hospitalServiceDTO.getId())
            .map(existingHospitalService -> {
                hospitalServiceMapper.partialUpdate(existingHospitalService, hospitalServiceDTO);

                return existingHospitalService;
            })
            .map(hospitalServiceRepository::save)
            .map(hospitalServiceMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HospitalServiceDTO> findAll() {
        LOG.debug("Request to get all HospitalServices");
        return hospitalServiceRepository
            .findAll()
            .stream()
            .map(hospitalServiceMapper::toDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<HospitalServiceDTO> findOne(Long id) {
        LOG.debug("Request to get HospitalService : {}", id);
        return hospitalServiceRepository.findById(id).map(hospitalServiceMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete HospitalService : {}", id);
        hospitalServiceRepository.deleteById(id);
    }
}
