package com.hyperbrains.hms.service.impl;

import com.hyperbrains.hms.domain.AdmissionTransfer;
import com.hyperbrains.hms.repository.AdmissionTransferRepository;
import com.hyperbrains.hms.service.AdmissionTransferService;
import com.hyperbrains.hms.service.dto.AdmissionTransferDTO;
import com.hyperbrains.hms.service.mapper.AdmissionTransferMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.hyperbrains.hms.domain.AdmissionTransfer}.
 */
@Service
@Transactional
public class AdmissionTransferServiceImpl implements AdmissionTransferService {

    private static final Logger LOG = LoggerFactory.getLogger(AdmissionTransferServiceImpl.class);

    private final AdmissionTransferRepository admissionTransferRepository;

    private final AdmissionTransferMapper admissionTransferMapper;

    public AdmissionTransferServiceImpl(
        AdmissionTransferRepository admissionTransferRepository,
        AdmissionTransferMapper admissionTransferMapper
    ) {
        this.admissionTransferRepository = admissionTransferRepository;
        this.admissionTransferMapper = admissionTransferMapper;
    }

    @Override
    public AdmissionTransferDTO save(AdmissionTransferDTO admissionTransferDTO) {
        LOG.debug("Request to save AdmissionTransfer : {}", admissionTransferDTO);
        AdmissionTransfer admissionTransfer = admissionTransferMapper.toEntity(admissionTransferDTO);
        admissionTransfer = admissionTransferRepository.save(admissionTransfer);
        return admissionTransferMapper.toDto(admissionTransfer);
    }

    @Override
    public AdmissionTransferDTO update(AdmissionTransferDTO admissionTransferDTO) {
        LOG.debug("Request to update AdmissionTransfer : {}", admissionTransferDTO);
        AdmissionTransfer admissionTransfer = admissionTransferMapper.toEntity(admissionTransferDTO);
        admissionTransfer = admissionTransferRepository.save(admissionTransfer);
        return admissionTransferMapper.toDto(admissionTransfer);
    }

    @Override
    public Optional<AdmissionTransferDTO> partialUpdate(AdmissionTransferDTO admissionTransferDTO) {
        LOG.debug("Request to partially update AdmissionTransfer : {}", admissionTransferDTO);

        return admissionTransferRepository
            .findById(admissionTransferDTO.getId())
            .map(existingAdmissionTransfer -> {
                admissionTransferMapper.partialUpdate(existingAdmissionTransfer, admissionTransferDTO);

                return existingAdmissionTransfer;
            })
            .map(admissionTransferRepository::save)
            .map(admissionTransferMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdmissionTransferDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all AdmissionTransfers");
        return admissionTransferRepository.findAll(pageable).map(admissionTransferMapper::toDto);
    }

    public Page<AdmissionTransferDTO> findAllWithEagerRelationships(Pageable pageable) {
        return admissionTransferRepository.findAllWithEagerRelationships(pageable).map(admissionTransferMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AdmissionTransferDTO> findOne(Long id) {
        LOG.debug("Request to get AdmissionTransfer : {}", id);
        return admissionTransferRepository.findOneWithEagerRelationships(id).map(admissionTransferMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete AdmissionTransfer : {}", id);
        admissionTransferRepository.deleteById(id);
    }
}
