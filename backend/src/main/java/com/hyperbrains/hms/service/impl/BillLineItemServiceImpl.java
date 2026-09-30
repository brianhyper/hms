package com.hyperbrains.hms.service.impl;

import com.hyperbrains.hms.domain.BillLineItem;
import com.hyperbrains.hms.repository.BillLineItemRepository;
import com.hyperbrains.hms.service.BillLineItemService;
import com.hyperbrains.hms.service.BusinessRuleViolationException;
import com.hyperbrains.hms.service.dto.BillLineItemDTO;
import com.hyperbrains.hms.service.mapper.BillLineItemMapper;
import com.hyperbrains.hms.service.rules.WorkflowOwnedFields;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.hyperbrains.hms.domain.BillLineItem}.
 */
@Service
@Transactional
public class BillLineItemServiceImpl implements BillLineItemService {

    private static final Logger LOG = LoggerFactory.getLogger(BillLineItemServiceImpl.class);

    private final BillLineItemRepository billLineItemRepository;

    private final BillLineItemMapper billLineItemMapper;

    public BillLineItemServiceImpl(BillLineItemRepository billLineItemRepository, BillLineItemMapper billLineItemMapper) {
        this.billLineItemRepository = billLineItemRepository;
        this.billLineItemMapper = billLineItemMapper;
    }

    /**
     * Everything on a bill line is money, or what the money was for.
     *
     * <p>These are the fields the billing workflow writes when a charge is earned and reverses when one is
     * voided. A hand-written update to the same columns is a charge nobody ordered, a price nobody agreed, or a
     * line re-pointed at a different bill — and it is the one kind of edit that no later report can detect,
     * because the totals would be internally consistent and wrong.
     */
    private static final String[] MONEY_FIELDS = { "description", "amount", "sourceType", "sourceRef", "bill" };

    /**
     * Refused: a charge is raised by the workflow that earned it.
     *
     * <p>A bill line comes from a consultation, a lab or radiology order, a pharmacy hand-over, or an agreed
     * extra charge. Creating one here would put money on a patient's bill with nothing behind it, and the
     * workflow that should have raised it would have no record that it did.
     */
    @Override
    public BillLineItemDTO save(BillLineItemDTO billLineItemDTO) {
        LOG.debug("Request to save BillLineItem : {}", billLineItemDTO);
        throw BusinessRuleViolationException.of(
            "billLineItemNotAddedByHand",
            "billLineItem",
            "A charge is raised by the workflow that earns it (consultation, lab, radiology, pharmacy, or an agreed extra charge), so a bill line cannot be created here"
        );
    }

    @Override
    public BillLineItemDTO update(BillLineItemDTO billLineItemDTO) {
        LOG.debug("Request to update BillLineItem : {}", billLineItemDTO);
        // A PUT carries the whole record, so an empty field here means "clear it" rather than "leave it".
        refuseMoneyChanges(billLineItemDTO, requireStored(billLineItemDTO.getId()), false);
        BillLineItem billLineItem = billLineItemMapper.toEntity(billLineItemDTO);
        billLineItem = billLineItemRepository.save(billLineItem);
        return billLineItemMapper.toDto(billLineItem);
    }

    @Override
    public Optional<BillLineItemDTO> partialUpdate(BillLineItemDTO billLineItemDTO) {
        LOG.debug("Request to partially update BillLineItem : {}", billLineItemDTO);

        return billLineItemRepository
            .findById(billLineItemDTO.getId())
            .map(existingBillLineItem -> {
                // A PATCH carries only what is changing, so an empty field here means "leave it".
                refuseMoneyChanges(billLineItemDTO, existingBillLineItem, true);
                billLineItemMapper.partialUpdate(existingBillLineItem, billLineItemDTO);

                return existingBillLineItem;
            })
            .map(billLineItemRepository::save)
            .map(billLineItemMapper::toDto);
    }

    /**
     * Refused: a bill line is voided, not deleted.
     *
     * <p>The record of what was charged has to survive the decision to stop charging for it. A void keeps the
     * line, who reversed it and why; a delete leaves the patient's bill with a total that no longer adds up, and
     * nothing to show what changed.
     */
    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete BillLineItem : {}", id);
        throw BusinessRuleViolationException.of(
            "billLineItemNotDeleted",
            "billLineItem",
            "A bill line is voided rather than deleted, so that the record of what was charged survives the decision to stop charging for it"
        );
    }

    private void refuseMoneyChanges(BillLineItemDTO requested, BillLineItem stored, boolean nullMeansUnchanged) {
        List<String> changed = WorkflowOwnedFields.changed(
            billLineItemMapper.toEntity(requested),
            stored,
            nullMeansUnchanged,
            MONEY_FIELDS
        );
        if (!changed.isEmpty()) {
            throw BusinessRuleViolationException.of(
                "billLineItemNotEditedByHand",
                "billLineItem",
                "A bill line is raised by a charge and reversed by a void, not edited; this request would change " +
                String.join(", ", changed)
            );
        }
    }

    private BillLineItem requireStored(Long id) {
        return billLineItemRepository
            .findById(id)
            .orElseThrow(() ->
                BusinessRuleViolationException.of("billLineItemNotFound", "billLineItem", "No bill line with id " + id)
            );
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillLineItemDTO> findAll() {
        LOG.debug("Request to get all BillLineItems");
        return billLineItemRepository.findAll().stream().map(billLineItemMapper::toDto).collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BillLineItemDTO> findOne(Long id) {
        LOG.debug("Request to get BillLineItem : {}", id);
        return billLineItemRepository.findById(id).map(billLineItemMapper::toDto);
    }
}
