package com.hyperbrains.hms.service.impl;

import com.hyperbrains.hms.domain.Referral;
import com.hyperbrains.hms.repository.ReferralRepository;
import com.hyperbrains.hms.service.ReferralService;
import com.hyperbrains.hms.service.dto.ReferralDTO;
import com.hyperbrains.hms.service.mapper.ReferralMapper;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.hyperbrains.hms.domain.Referral}.
 */
@Service
@Transactional
public class ReferralServiceImpl implements ReferralService {

    private static final Logger LOG = LoggerFactory.getLogger(ReferralServiceImpl.class);

    private final ReferralRepository referralRepository;

    private final ReferralMapper referralMapper;

    public ReferralServiceImpl(ReferralRepository referralRepository, ReferralMapper referralMapper) {
        this.referralRepository = referralRepository;
        this.referralMapper = referralMapper;
    }

    @Override
    public ReferralDTO save(ReferralDTO referralDTO) {
        LOG.debug("Request to save Referral : {}", referralDTO);
        Referral referral = referralMapper.toEntity(referralDTO);
        referral = referralRepository.save(referral);
        return referralMapper.toDto(referral);
    }

    @Override
    public ReferralDTO update(ReferralDTO referralDTO) {
        LOG.debug("Request to update Referral : {}", referralDTO);
        Referral referral = referralMapper.toEntity(referralDTO);
        referral = referralRepository.save(referral);
        return referralMapper.toDto(referral);
    }

    @Override
    public Optional<ReferralDTO> partialUpdate(ReferralDTO referralDTO) {
        LOG.debug("Request to partially update Referral : {}", referralDTO);

        return referralRepository
            .findById(referralDTO.getId())
            .map(existingReferral -> {
                referralMapper.partialUpdate(existingReferral, referralDTO);

                return existingReferral;
            })
            .map(referralRepository::save)
            .map(referralMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReferralDTO> findAll() {
        LOG.debug("Request to get all Referrals");
        return referralRepository.findAll().stream().map(referralMapper::toDto).collect(Collectors.toCollection(LinkedList::new));
    }

    public Page<ReferralDTO> findAllWithEagerRelationships(Pageable pageable) {
        return referralRepository.findAllWithEagerRelationships(pageable).map(referralMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReferralDTO> findOne(Long id) {
        LOG.debug("Request to get Referral : {}", id);
        return referralRepository.findOneWithEagerRelationships(id).map(referralMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Referral : {}", id);
        referralRepository.deleteById(id);
    }
}
