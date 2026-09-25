package com.hyperbrains.hms.service.impl;

import com.hyperbrains.hms.domain.PaymentPlan;
import com.hyperbrains.hms.repository.PaymentPlanRepository;
import com.hyperbrains.hms.service.PaymentPlanService;
import com.hyperbrains.hms.service.dto.PaymentPlanDTO;
import com.hyperbrains.hms.service.mapper.PaymentPlanMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.hyperbrains.hms.domain.PaymentPlan}.
 */
@Service
@Transactional
public class PaymentPlanServiceImpl implements PaymentPlanService {

    private static final Logger LOG = LoggerFactory.getLogger(PaymentPlanServiceImpl.class);

    private final PaymentPlanRepository paymentPlanRepository;

    private final PaymentPlanMapper paymentPlanMapper;

    public PaymentPlanServiceImpl(PaymentPlanRepository paymentPlanRepository, PaymentPlanMapper paymentPlanMapper) {
        this.paymentPlanRepository = paymentPlanRepository;
        this.paymentPlanMapper = paymentPlanMapper;
    }

    @Override
    public PaymentPlanDTO save(PaymentPlanDTO paymentPlanDTO) {
        LOG.debug("Request to save PaymentPlan : {}", paymentPlanDTO);
        PaymentPlan paymentPlan = paymentPlanMapper.toEntity(paymentPlanDTO);
        paymentPlan = paymentPlanRepository.save(paymentPlan);
        return paymentPlanMapper.toDto(paymentPlan);
    }

    @Override
    public PaymentPlanDTO update(PaymentPlanDTO paymentPlanDTO) {
        LOG.debug("Request to update PaymentPlan : {}", paymentPlanDTO);
        PaymentPlan paymentPlan = paymentPlanMapper.toEntity(paymentPlanDTO);
        paymentPlan = paymentPlanRepository.save(paymentPlan);
        return paymentPlanMapper.toDto(paymentPlan);
    }

    @Override
    public Optional<PaymentPlanDTO> partialUpdate(PaymentPlanDTO paymentPlanDTO) {
        LOG.debug("Request to partially update PaymentPlan : {}", paymentPlanDTO);

        return paymentPlanRepository
            .findById(paymentPlanDTO.getId())
            .map(existingPaymentPlan -> {
                paymentPlanMapper.partialUpdate(existingPaymentPlan, paymentPlanDTO);

                return existingPaymentPlan;
            })
            .map(paymentPlanRepository::save)
            .map(paymentPlanMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentPlanDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all PaymentPlans");
        return paymentPlanRepository.findAll(pageable).map(paymentPlanMapper::toDto);
    }

    public Page<PaymentPlanDTO> findAllWithEagerRelationships(Pageable pageable) {
        return paymentPlanRepository.findAllWithEagerRelationships(pageable).map(paymentPlanMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PaymentPlanDTO> findOne(Long id) {
        LOG.debug("Request to get PaymentPlan : {}", id);
        return paymentPlanRepository.findOneWithEagerRelationships(id).map(paymentPlanMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete PaymentPlan : {}", id);
        paymentPlanRepository.deleteById(id);
    }
}
