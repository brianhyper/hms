package com.hyperbrains.hms.service.impl;

import com.hyperbrains.hms.domain.Bed;
import com.hyperbrains.hms.repository.BedRepository;
import com.hyperbrains.hms.service.BedService;
import com.hyperbrains.hms.service.dto.BedDTO;
import com.hyperbrains.hms.service.mapper.BedMapper;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.hyperbrains.hms.domain.Bed}.
 */
@Service
@Transactional
public class BedServiceImpl implements BedService {

    private static final Logger LOG = LoggerFactory.getLogger(BedServiceImpl.class);

    private final BedRepository bedRepository;

    private final BedMapper bedMapper;

    public BedServiceImpl(BedRepository bedRepository, BedMapper bedMapper) {
        this.bedRepository = bedRepository;
        this.bedMapper = bedMapper;
    }

    @Override
    public BedDTO save(BedDTO bedDTO) {
        LOG.debug("Request to save Bed : {}", bedDTO);
        Bed bed = bedMapper.toEntity(bedDTO);
        bed = bedRepository.save(bed);
        return bedMapper.toDto(bed);
    }

    @Override
    public BedDTO update(BedDTO bedDTO) {
        LOG.debug("Request to update Bed : {}", bedDTO);
        Bed bed = bedMapper.toEntity(bedDTO);
        bed = bedRepository.save(bed);
        return bedMapper.toDto(bed);
    }

    @Override
    public Optional<BedDTO> partialUpdate(BedDTO bedDTO) {
        LOG.debug("Request to partially update Bed : {}", bedDTO);

        return bedRepository
            .findById(bedDTO.getId())
            .map(existingBed -> {
                bedMapper.partialUpdate(existingBed, bedDTO);

                return existingBed;
            })
            .map(bedRepository::save)
            .map(bedMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BedDTO> findAll() {
        LOG.debug("Request to get all Beds");
        return bedRepository.findAll().stream().map(bedMapper::toDto).collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BedDTO> findOne(Long id) {
        LOG.debug("Request to get Bed : {}", id);
        return bedRepository.findById(id).map(bedMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Bed : {}", id);
        bedRepository.deleteById(id);
    }
}
