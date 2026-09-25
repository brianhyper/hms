package com.hyperbrains.hms.service.impl;

import com.hyperbrains.hms.domain.BillLineItem;
import com.hyperbrains.hms.repository.BillLineItemRepository;
import com.hyperbrains.hms.service.BillLineItemService;
import com.hyperbrains.hms.service.dto.BillLineItemDTO;
import com.hyperbrains.hms.service.mapper.BillLineItemMapper;
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

    @Override
    public BillLineItemDTO save(BillLineItemDTO billLineItemDTO) {
        LOG.debug("Request to save BillLineItem : {}", billLineItemDTO);
        BillLineItem billLineItem = billLineItemMapper.toEntity(billLineItemDTO);
        billLineItem = billLineItemRepository.save(billLineItem);
        return billLineItemMapper.toDto(billLineItem);
    }

    @Override
    public BillLineItemDTO update(BillLineItemDTO billLineItemDTO) {
        LOG.debug("Request to update BillLineItem : {}", billLineItemDTO);
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
                billLineItemMapper.partialUpdate(existingBillLineItem, billLineItemDTO);

                return existingBillLineItem;
            })
            .map(billLineItemRepository::save)
            .map(billLineItemMapper::toDto);
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

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete BillLineItem : {}", id);
        billLineItemRepository.deleteById(id);
    }
}
