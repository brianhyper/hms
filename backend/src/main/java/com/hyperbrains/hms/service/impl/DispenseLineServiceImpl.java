package com.hyperbrains.hms.service.impl;

import com.hyperbrains.hms.domain.DispenseLine;
import com.hyperbrains.hms.repository.DispenseLineRepository;
import com.hyperbrains.hms.service.DispenseLineService;
import com.hyperbrains.hms.service.dto.DispenseLineDTO;
import com.hyperbrains.hms.service.mapper.DispenseLineMapper;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.hyperbrains.hms.domain.DispenseLine}.
 */
@Service
@Transactional
public class DispenseLineServiceImpl implements DispenseLineService {

    private static final Logger LOG = LoggerFactory.getLogger(DispenseLineServiceImpl.class);

    private final DispenseLineRepository dispenseLineRepository;

    private final DispenseLineMapper dispenseLineMapper;

    public DispenseLineServiceImpl(DispenseLineRepository dispenseLineRepository, DispenseLineMapper dispenseLineMapper) {
        this.dispenseLineRepository = dispenseLineRepository;
        this.dispenseLineMapper = dispenseLineMapper;
    }

    @Override
    public DispenseLineDTO save(DispenseLineDTO dispenseLineDTO) {
        LOG.debug("Request to save DispenseLine : {}", dispenseLineDTO);
        DispenseLine dispenseLine = dispenseLineMapper.toEntity(dispenseLineDTO);
        dispenseLine = dispenseLineRepository.save(dispenseLine);
        return dispenseLineMapper.toDto(dispenseLine);
    }

    @Override
    public DispenseLineDTO update(DispenseLineDTO dispenseLineDTO) {
        LOG.debug("Request to update DispenseLine : {}", dispenseLineDTO);
        DispenseLine dispenseLine = dispenseLineMapper.toEntity(dispenseLineDTO);
        dispenseLine = dispenseLineRepository.save(dispenseLine);
        return dispenseLineMapper.toDto(dispenseLine);
    }

    @Override
    public Optional<DispenseLineDTO> partialUpdate(DispenseLineDTO dispenseLineDTO) {
        LOG.debug("Request to partially update DispenseLine : {}", dispenseLineDTO);

        return dispenseLineRepository
            .findById(dispenseLineDTO.getId())
            .map(existingDispenseLine -> {
                dispenseLineMapper.partialUpdate(existingDispenseLine, dispenseLineDTO);

                return existingDispenseLine;
            })
            .map(dispenseLineRepository::save)
            .map(dispenseLineMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DispenseLineDTO> findAll() {
        LOG.debug("Request to get all DispenseLines");
        return dispenseLineRepository.findAll().stream().map(dispenseLineMapper::toDto).collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DispenseLineDTO> findOne(Long id) {
        LOG.debug("Request to get DispenseLine : {}", id);
        return dispenseLineRepository.findById(id).map(dispenseLineMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete DispenseLine : {}", id);
        dispenseLineRepository.deleteById(id);
    }
}
