package com.hyperbrains.hms.service.impl;

import com.hyperbrains.hms.domain.Drug;
import com.hyperbrains.hms.repository.DrugRepository;
import com.hyperbrains.hms.service.DrugService;
import com.hyperbrains.hms.service.dto.DrugDTO;
import com.hyperbrains.hms.service.mapper.DrugMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.hyperbrains.hms.domain.Drug}.
 */
@Service
@Transactional
public class DrugServiceImpl implements DrugService {

    private static final Logger LOG = LoggerFactory.getLogger(DrugServiceImpl.class);

    private final DrugRepository drugRepository;

    private final DrugMapper drugMapper;

    public DrugServiceImpl(DrugRepository drugRepository, DrugMapper drugMapper) {
        this.drugRepository = drugRepository;
        this.drugMapper = drugMapper;
    }

    @Override
    public DrugDTO save(DrugDTO drugDTO) {
        LOG.debug("Request to save Drug : {}", drugDTO);
        Drug drug = drugMapper.toEntity(drugDTO);
        drug = drugRepository.save(drug);
        return drugMapper.toDto(drug);
    }

    @Override
    public DrugDTO update(DrugDTO drugDTO) {
        LOG.debug("Request to update Drug : {}", drugDTO);
        Drug drug = drugMapper.toEntity(drugDTO);
        drug = drugRepository.save(drug);
        return drugMapper.toDto(drug);
    }

    @Override
    public Optional<DrugDTO> partialUpdate(DrugDTO drugDTO) {
        LOG.debug("Request to partially update Drug : {}", drugDTO);

        return drugRepository
            .findById(drugDTO.getId())
            .map(existingDrug -> {
                drugMapper.partialUpdate(existingDrug, drugDTO);

                return existingDrug;
            })
            .map(drugRepository::save)
            .map(drugMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DrugDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Drugs");
        return drugRepository.findAll(pageable).map(drugMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DrugDTO> findOne(Long id) {
        LOG.debug("Request to get Drug : {}", id);
        return drugRepository.findById(id).map(drugMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Drug : {}", id);
        drugRepository.deleteById(id);
    }
}
