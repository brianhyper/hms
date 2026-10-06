package com.hyperbrains.hms.service.impl;

import com.hyperbrains.hms.domain.Bill;
import com.hyperbrains.hms.repository.BillRepository;
import com.hyperbrains.hms.service.BillService;
import com.hyperbrains.hms.service.BusinessRuleViolationException;
import com.hyperbrains.hms.service.dto.BillDTO;
import com.hyperbrains.hms.service.mapper.BillMapper;
import com.hyperbrains.hms.service.rules.WorkflowOwnedFields;
import java.util.ArrayList;
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
 * Service Implementation for managing {@link com.hyperbrains.hms.domain.Bill}.
 */
@Service
@Transactional
public class BillServiceImpl implements BillService {

    private static final Logger LOG = LoggerFactory.getLogger(BillServiceImpl.class);

    private final BillRepository billRepository;

    private final BillMapper billMapper;

    public BillServiceImpl(BillRepository billRepository, BillMapper billMapper) {
        this.billRepository = billRepository;
        this.billMapper = billMapper;
    }

    /**
     * What the billing workflow writes, and what a hand-written update must not touch.
     *
     * <p>{@code totalAmount} is the sum of the bill's lines and {@code paidAt} is the moment settlement completed.
     * A hand edit to either leaves a bill whose total does not follow from its own lines, or one that says it was
     * settled by a payment that no payment row records — which is exactly the kind of figure that survives every
     * later report, because nothing downstream re-derives it.
     *
     * <p>The payment that settled the bill is guarded too, but separately and by id rather than by field name: a
     * reference cannot go in this list, because the stored one is a proxy and this guard compares by identity. See
     * {@link WorkflowOwnedFields#referenceChanged}.
     */
    private static final String[] WORKFLOW_OWNED_FIELDS = { "totalAmount", "paidAt", "status" };

    @Override
    public BillDTO save(BillDTO billDTO) {
        LOG.debug("Request to save Bill : {}", billDTO);
        Bill bill = billMapper.toEntity(billDTO);
        bill = billRepository.save(bill);
        return billMapper.toDto(bill);
    }

    @Override
    public BillDTO update(BillDTO billDTO) {
        LOG.debug("Request to update Bill : {}", billDTO);
        // A PUT carries the whole record, so an empty field here means "clear it" rather than "leave it".
        refuseHandEdits(billDTO, requireStored(billDTO.getId()), false);
        Bill bill = billMapper.toEntity(billDTO);
        bill = billRepository.save(bill);
        return billMapper.toDto(bill);
    }

    @Override
    public Optional<BillDTO> partialUpdate(BillDTO billDTO) {
        LOG.debug("Request to partially update Bill : {}", billDTO);

        return billRepository
            .findById(billDTO.getId())
            .map(existingBill -> {
                // A PATCH carries only what is changing, so an empty field here means "leave it".
                refuseHandEdits(billDTO, existingBill, true);
                billMapper.partialUpdate(existingBill, billDTO);

                return existingBill;
            })
            .map(billRepository::save)
            .map(billMapper::toDto);
    }

    private void refuseHandEdits(BillDTO requested, Bill stored, boolean nullMeansUnchanged) {
        List<String> changed = new ArrayList<>(
            WorkflowOwnedFields.changed(billMapper.toEntity(requested), stored, nullMeansUnchanged, WORKFLOW_OWNED_FIELDS)
        );

        // The payment is compared by id, not as an entity: the stored one is a Hibernate proxy and reading an id off it
        // forces a load. The request carries the id as a plain value and the stored id comes from a scalar projection,
        // so neither side touches a proxy and a legitimate edit that re-sends the same payment is not mistaken for a
        // re-point.
        BillRepository.ReferenceIds storedIds = billRepository
            .findReferenceIds(stored.getId())
            .orElseThrow(() -> BusinessRuleViolationException.of("billNotFound", "bill", "No bill with id " + stored.getId()));
        if (WorkflowOwnedFields.referenceChanged(idOfPayment(requested), storedIds.getPaymentId(), nullMeansUnchanged)) {
            changed.add("payment");
        }

        if (!changed.isEmpty()) {
            throw BusinessRuleViolationException.of(
                "billNotEditedByHand",
                "bill",
                "A bill's total follows from its lines and is settled by a payment, so neither is edited here; this request would change " +
                String.join(", ", changed)
            );
        }
    }

    private static Long idOfPayment(BillDTO requested) {
        return requested.getPayment() == null ? null : requested.getPayment().getId();
    }

    private Bill requireStored(Long id) {
        return billRepository
            .findById(id)
            .orElseThrow(() -> BusinessRuleViolationException.of("billNotFound", "bill", "No bill with id " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BillDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Bills");
        return billRepository.findAll(pageable).map(billMapper::toDto);
    }

    /**
     *  Get all the bills where Visit is {@code null}.
     *  @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<BillDTO> findAllWhereVisitIsNull() {
        LOG.debug("Request to get all bills where Visit is null");
        return StreamSupport.stream(billRepository.findAll().spliterator(), false)
            .filter(bill -> bill.getVisit() == null)
            .map(billMapper::toDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BillDTO> findOne(Long id) {
        LOG.debug("Request to get Bill : {}", id);
        return billRepository.findById(id).map(billMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Bill : {}", id);
        billRepository.deleteById(id);
    }
}
