package com.hyperbrains.hms.service.impl;

import com.hyperbrains.hms.domain.AdHocCharge;
import com.hyperbrains.hms.repository.AdHocChargeRepository;
import com.hyperbrains.hms.service.AdHocChargeService;
import com.hyperbrains.hms.service.dto.AdHocChargeDTO;
import com.hyperbrains.hms.service.mapper.AdHocChargeMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.hyperbrains.hms.domain.AdHocCharge}.
 */
@Service
@Transactional
public class AdHocChargeServiceImpl implements AdHocChargeService {

    private static final Logger LOG = LoggerFactory.getLogger(AdHocChargeServiceImpl.class);

    private final AdHocChargeRepository adHocChargeRepository;

    private final AdHocChargeMapper adHocChargeMapper;

    public AdHocChargeServiceImpl(AdHocChargeRepository adHocChargeRepository, AdHocChargeMapper adHocChargeMapper) {
        this.adHocChargeRepository = adHocChargeRepository;
        this.adHocChargeMapper = adHocChargeMapper;
    }

    @Override
    public AdHocChargeDTO save(AdHocChargeDTO adHocChargeDTO) {
        LOG.debug("Request to save AdHocCharge : {}", adHocChargeDTO);
        AdHocCharge adHocCharge = adHocChargeMapper.toEntity(adHocChargeDTO);
        adHocCharge = adHocChargeRepository.save(adHocCharge);
        return adHocChargeMapper.toDto(adHocCharge);
    }

    @Override
    public AdHocChargeDTO update(AdHocChargeDTO adHocChargeDTO) {
        LOG.debug("Request to update AdHocCharge : {}", adHocChargeDTO);
        AdHocCharge adHocCharge = adHocChargeMapper.toEntity(adHocChargeDTO);
        adHocCharge = adHocChargeRepository.save(adHocCharge);
        return adHocChargeMapper.toDto(adHocCharge);
    }

    @Override
    public Optional<AdHocChargeDTO> partialUpdate(AdHocChargeDTO adHocChargeDTO) {
        LOG.debug("Request to partially update AdHocCharge : {}", adHocChargeDTO);

        return adHocChargeRepository
            .findById(adHocChargeDTO.getId())
            .map(existingAdHocCharge -> {
                adHocChargeMapper.partialUpdate(existingAdHocCharge, adHocChargeDTO);

                return existingAdHocCharge;
            })
            .map(adHocChargeRepository::save)
            .map(adHocChargeMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdHocChargeDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all AdHocCharges");
        return adHocChargeRepository.findAll(pageable).map(adHocChargeMapper::toDto);
    }

    public Page<AdHocChargeDTO> findAllWithEagerRelationships(Pageable pageable) {
        return adHocChargeRepository.findAllWithEagerRelationships(pageable).map(adHocChargeMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AdHocChargeDTO> findOne(Long id) {
        LOG.debug("Request to get AdHocCharge : {}", id);
        return adHocChargeRepository.findOneWithEagerRelationships(id).map(adHocChargeMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete AdHocCharge : {}", id);
        adHocChargeRepository.deleteById(id);
    }
}
