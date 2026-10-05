package com.hyperbrains.hms.service.impl;

import com.hyperbrains.hms.domain.Payment;
import com.hyperbrains.hms.repository.PaymentRepository;
import com.hyperbrains.hms.service.BusinessRuleViolationException;
import com.hyperbrains.hms.service.PaymentService;
import com.hyperbrains.hms.service.dto.PaymentDTO;
import com.hyperbrains.hms.service.mapper.PaymentMapper;
import com.hyperbrains.hms.service.rules.WorkflowOwnedFields;
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
 * Service Implementation for managing {@link com.hyperbrains.hms.domain.Payment}.
 */
@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private static final Logger LOG = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final PaymentRepository paymentRepository;

    private final PaymentMapper paymentMapper;

    public PaymentServiceImpl(PaymentRepository paymentRepository, PaymentMapper paymentMapper) {
        this.paymentRepository = paymentRepository;
        this.paymentMapper = paymentMapper;
    }

    /**
     * What the payment workflow writes when money is taken, and what a hand-written update must not touch.
     *
     * <p>{@code amount} is how much was handed over and {@code recordedAt} is when the counter took it. Both are the
     * record of a transaction that happened, so an edit to either is a payment that never took place — and it is the
     * kind of change no later report can detect, because a forged payment is internally consistent. The way to undo
     * money received is a refund through the billing workflow, which leaves both the payment and its reversal on the
     * record.
     */
    private static final String[] WORKFLOW_OWNED_FIELDS = { "amount", "recordedAt" };

    @Override
    public PaymentDTO save(PaymentDTO paymentDTO) {
        LOG.debug("Request to save Payment : {}", paymentDTO);
        Payment payment = paymentMapper.toEntity(paymentDTO);
        payment = paymentRepository.save(payment);
        return paymentMapper.toDto(payment);
    }

    @Override
    public PaymentDTO update(PaymentDTO paymentDTO) {
        LOG.debug("Request to update Payment : {}", paymentDTO);
        // A PUT carries the whole record, so an empty field here means "clear it" rather than "leave it".
        refuseHandEdits(paymentDTO, requireStored(paymentDTO.getId()), false);
        Payment payment = paymentMapper.toEntity(paymentDTO);
        payment = paymentRepository.save(payment);
        return paymentMapper.toDto(payment);
    }

    @Override
    public Optional<PaymentDTO> partialUpdate(PaymentDTO paymentDTO) {
        LOG.debug("Request to partially update Payment : {}", paymentDTO);

        return paymentRepository
            .findById(paymentDTO.getId())
            .map(existingPayment -> {
                // A PATCH carries only what is changing, so an empty field here means "leave it".
                refuseHandEdits(paymentDTO, existingPayment, true);
                paymentMapper.partialUpdate(existingPayment, paymentDTO);

                return existingPayment;
            })
            .map(paymentRepository::save)
            .map(paymentMapper::toDto);
    }

    private void refuseHandEdits(PaymentDTO requested, Payment stored, boolean nullMeansUnchanged) {
        List<String> changed = WorkflowOwnedFields.changed(
            paymentMapper.toEntity(requested),
            stored,
            nullMeansUnchanged,
            WORKFLOW_OWNED_FIELDS
        );
        if (!changed.isEmpty()) {
            throw BusinessRuleViolationException.of(
                "paymentNotEditedByHand",
                "payment",
                "A payment records money that was taken, and is not edited; this request would change " + String.join(", ", changed)
            );
        }
    }

    private Payment requireStored(Long id) {
        return paymentRepository
            .findById(id)
            .orElseThrow(() -> BusinessRuleViolationException.of("paymentNotFound", "payment", "No payment with id " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Payments");
        return paymentRepository.findAll(pageable).map(paymentMapper::toDto);
    }

    public Page<PaymentDTO> findAllWithEagerRelationships(Pageable pageable) {
        return paymentRepository.findAllWithEagerRelationships(pageable).map(paymentMapper::toDto);
    }

    /**
     *  Get all the payments where Bill is {@code null}.
     *  @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<PaymentDTO> findAllWhereBillIsNull() {
        LOG.debug("Request to get all payments where Bill is null");
        return StreamSupport.stream(paymentRepository.findAll().spliterator(), false)
            .filter(payment -> payment.getBill() == null)
            .map(paymentMapper::toDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PaymentDTO> findOne(Long id) {
        LOG.debug("Request to get Payment : {}", id);
        return paymentRepository.findOneWithEagerRelationships(id).map(paymentMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Payment : {}", id);
        paymentRepository.deleteById(id);
    }
}
