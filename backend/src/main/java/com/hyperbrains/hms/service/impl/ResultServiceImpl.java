package com.hyperbrains.hms.service.impl;

import com.hyperbrains.hms.domain.Result;
import com.hyperbrains.hms.repository.ResultRepository;
import com.hyperbrains.hms.service.ResultService;
import com.hyperbrains.hms.service.dto.ResultDTO;
import com.hyperbrains.hms.service.mapper.ResultMapper;
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
 * Service Implementation for managing {@link com.hyperbrains.hms.domain.Result}.
 */
@Service
@Transactional
public class ResultServiceImpl implements ResultService {

    private static final Logger LOG = LoggerFactory.getLogger(ResultServiceImpl.class);

    private final ResultRepository resultRepository;

    private final ResultMapper resultMapper;

    public ResultServiceImpl(ResultRepository resultRepository, ResultMapper resultMapper) {
        this.resultRepository = resultRepository;
        this.resultMapper = resultMapper;
    }

    @Override
    public ResultDTO save(ResultDTO resultDTO) {
        LOG.debug("Request to save Result : {}", resultDTO);
        Result result = resultMapper.toEntity(resultDTO);
        result = resultRepository.save(result);
        return resultMapper.toDto(result);
    }

    @Override
    public ResultDTO update(ResultDTO resultDTO) {
        LOG.debug("Request to update Result : {}", resultDTO);
        Result result = resultMapper.toEntity(resultDTO);
        result = resultRepository.save(result);
        return resultMapper.toDto(result);
    }

    @Override
    public Optional<ResultDTO> partialUpdate(ResultDTO resultDTO) {
        LOG.debug("Request to partially update Result : {}", resultDTO);

        return resultRepository
            .findById(resultDTO.getId())
            .map(existingResult -> {
                resultMapper.partialUpdate(existingResult, resultDTO);

                return existingResult;
            })
            .map(resultRepository::save)
            .map(resultMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResultDTO> findAll() {
        LOG.debug("Request to get all Results");
        return resultRepository.findAll().stream().map(resultMapper::toDto).collect(Collectors.toCollection(LinkedList::new));
    }

    public Page<ResultDTO> findAllWithEagerRelationships(Pageable pageable) {
        return resultRepository.findAllWithEagerRelationships(pageable).map(resultMapper::toDto);
    }

    /**
     *  Get all the results where Order is {@code null}.
     *  @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<ResultDTO> findAllWhereOrderIsNull() {
        LOG.debug("Request to get all results where Order is null");
        return StreamSupport.stream(resultRepository.findAll().spliterator(), false)
            .filter(result -> result.getOrder() == null)
            .map(resultMapper::toDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ResultDTO> findOne(Long id) {
        LOG.debug("Request to get Result : {}", id);
        return resultRepository.findOneWithEagerRelationships(id).map(resultMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Result : {}", id);
        resultRepository.deleteById(id);
    }
}
