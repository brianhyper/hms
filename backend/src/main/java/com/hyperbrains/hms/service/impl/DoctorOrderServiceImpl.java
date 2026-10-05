package com.hyperbrains.hms.service.impl;

import com.hyperbrains.hms.domain.DoctorOrder;
import com.hyperbrains.hms.repository.DoctorOrderRepository;
import com.hyperbrains.hms.service.BusinessRuleViolationException;
import com.hyperbrains.hms.service.DoctorOrderService;
import com.hyperbrains.hms.service.dto.DoctorOrderDTO;
import com.hyperbrains.hms.service.mapper.DoctorOrderMapper;
import com.hyperbrains.hms.service.rules.WorkflowOwnedFields;
import java.util.List;
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

    /**
     * Where the order stands, and when it was stopped, both written by the order's own operations.
     *
     * <p>{@code status} decides whether the order is still to be carried out, so setting it by hand is how a
     * cancelled order quietly becomes an active one, or the reverse, with nobody's name against the decision — the
     * order operations record who cancelled it and why. {@code cancelledAt} is the same decision's timestamp.
     */
    private static final String[] WORKFLOW_OWNED_FIELDS = { "status", "cancelledAt" };

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
        // A PUT carries the whole record, so an empty field here means "clear it" rather than "leave it".
        refuseHandEdits(doctorOrderDTO, requireStored(doctorOrderDTO.getId()), false);
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
                // A PATCH carries only what is changing, so an empty field here means "leave it".
                refuseHandEdits(doctorOrderDTO, existingDoctorOrder, true);
                doctorOrderMapper.partialUpdate(existingDoctorOrder, doctorOrderDTO);

                return existingDoctorOrder;
            })
            .map(doctorOrderRepository::save)
            .map(doctorOrderMapper::toDto);
    }

    private void refuseHandEdits(DoctorOrderDTO requested, DoctorOrder stored, boolean nullMeansUnchanged) {
        List<String> changed = WorkflowOwnedFields.changed(
            doctorOrderMapper.toEntity(requested),
            stored,
            nullMeansUnchanged,
            WORKFLOW_OWNED_FIELDS
        );
        if (!changed.isEmpty()) {
            throw BusinessRuleViolationException.of(
                "doctorOrderNotEditedByHand",
                "doctorOrder",
                "An order is started, executed or cancelled by its own operations, which record who decided and why; this request would change " +
                String.join(", ", changed)
            );
        }
    }

    private DoctorOrder requireStored(Long id) {
        return doctorOrderRepository
            .findById(id)
            .orElseThrow(() -> BusinessRuleViolationException.of("doctorOrderNotFound", "doctorOrder", "No order with id " + id));
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
