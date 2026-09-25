package com.hyperbrains.hms.service.impl;

import com.hyperbrains.hms.domain.Bill;
import com.hyperbrains.hms.repository.BillRepository;
import com.hyperbrains.hms.service.BillService;
import com.hyperbrains.hms.service.dto.BillDTO;
import com.hyperbrains.hms.service.mapper.BillMapper;
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
                billMapper.partialUpdate(existingBill, billDTO);

                return existingBill;
            })
            .map(billRepository::save)
            .map(billMapper::toDto);
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
