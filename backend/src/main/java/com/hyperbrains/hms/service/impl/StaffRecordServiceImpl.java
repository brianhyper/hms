package com.hyperbrains.hms.service.impl;

import com.hyperbrains.hms.domain.StaffRecord;
import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.repository.StaffRecordRepository;
import com.hyperbrains.hms.service.BusinessRuleViolationException;
import com.hyperbrains.hms.service.StaffRecordService;
import com.hyperbrains.hms.service.dto.StaffRecordDTO;
import com.hyperbrains.hms.service.mapper.StaffRecordMapper;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.hyperbrains.hms.domain.StaffRecord}.
 *
 * <p>The two things a staff file has to get right, and the only logic in this class: one person is one record, and
 * one account belongs to one person. Both are checked here rather than left to the unique indexes, so the caller is
 * told which of the two it broke and why, in a 409 with a key the client can act on, instead of being handed a
 * database constraint violation it has to interpret.
 */
@Service
@Transactional
public class StaffRecordServiceImpl implements StaffRecordService {

    private static final Logger LOG = LoggerFactory.getLogger(StaffRecordServiceImpl.class);

    private final StaffRecordRepository staffRecordRepository;

    private final StaffRecordMapper staffRecordMapper;

    public StaffRecordServiceImpl(StaffRecordRepository staffRecordRepository, StaffRecordMapper staffRecordMapper) {
        this.staffRecordRepository = staffRecordRepository;
        this.staffRecordMapper = staffRecordMapper;
    }

    @Override
    public StaffRecordDTO save(StaffRecordDTO staffRecordDTO) {
        LOG.debug("Request to save StaffRecord : {}", staffRecordDTO);
        StaffRecord staffRecord = staffRecordMapper.toEntity(staffRecordDTO);
        refuseADuplicate(staffRecord, null);
        staffRecord = staffRecordRepository.save(staffRecord);
        return staffRecordMapper.toDto(staffRecord);
    }

    @Override
    public StaffRecordDTO update(StaffRecordDTO staffRecordDTO) {
        LOG.debug("Request to update StaffRecord : {}", staffRecordDTO);
        StaffRecord staffRecord = staffRecordMapper.toEntity(staffRecordDTO);
        refuseADuplicate(staffRecord, staffRecord.getId());
        staffRecord = staffRecordRepository.save(staffRecord);
        return staffRecordMapper.toDto(staffRecord);
    }

    @Override
    public Optional<StaffRecordDTO> partialUpdate(StaffRecordDTO staffRecordDTO) {
        LOG.debug("Request to partially update StaffRecord : {}", staffRecordDTO);

        return staffRecordRepository
            .findById(staffRecordDTO.getId())
            .map(existingStaffRecord -> {
                staffRecordMapper.partialUpdate(existingStaffRecord, staffRecordDTO);
                // Checked after the merge, not on the request: a partial update that sends only an identity number,
                // or only an account, is enough to introduce a duplicate, and what is stored is what matters.
                refuseADuplicate(existingStaffRecord, existingStaffRecord.getId());
                return existingStaffRecord;
            })
            .map(staffRecordRepository::save)
            .map(staffRecordMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StaffRecordDTO> findAll() {
        LOG.debug("Request to get all StaffRecords");
        return staffRecordRepository.findAll().stream().map(staffRecordMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<StaffRecordDTO> findOne(Long id) {
        LOG.debug("Request to get StaffRecord : {}", id);
        return staffRecordRepository.findById(id).map(staffRecordMapper::toDto);
    }

    /**
     * Refuses a record that would put a person on file twice, or give one account to two people.
     *
     * @param candidate the record as it would be stored
     * @param recordBeingChanged the record's own id when it is an edit, so a record does not clash with itself; null
     *                           when it is new
     */
    private void refuseADuplicate(StaffRecord candidate, Long recordBeingChanged) {
        staffRecordRepository
            .findOneByNationalId(candidate.getNationalId())
            .filter(existing -> isSomebodyElse(existing, recordBeingChanged))
            .ifPresent(existing -> {
                throw BusinessRuleViolationException.of(
                    "nationalIdAlreadyRecorded",
                    "staffRecord",
                    (
                        "Identity number " +
                        candidate.getNationalId() +
                        " is already on the record for " +
                        existing.getFullName() +
                        ". One person is one record; correct that one instead."
                    )
                );
            });

        User account = candidate.getUser();
        if (account == null) {
            return;
        }
        staffRecordRepository
            .findOneByUserId(account.getId())
            .filter(existing -> isSomebodyElse(existing, recordBeingChanged))
            .ifPresent(existing -> {
                throw BusinessRuleViolationException.of(
                    "userAlreadyOnStaffRecord",
                    "staffRecord",
                    (
                        "That account is already the login for " +
                        existing.getFullName() +
                        ". One account belongs to one person, or the rota, the leave balance and the payslip end up " +
                        "attributed to whoever is read first."
                    )
                );
            });
    }

    private static boolean isSomebodyElse(StaffRecord existing, Long recordBeingChanged) {
        return recordBeingChanged == null || !existing.getId().equals(recordBeingChanged);
    }
}
