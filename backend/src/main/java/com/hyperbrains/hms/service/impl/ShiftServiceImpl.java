package com.hyperbrains.hms.service.impl;

import com.hyperbrains.hms.domain.Shift;
import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.repository.ShiftRepository;
import com.hyperbrains.hms.repository.UserRepository;
import com.hyperbrains.hms.security.SecurityUtils;
import com.hyperbrains.hms.service.BusinessRuleViolationException;
import com.hyperbrains.hms.service.ShiftService;
import com.hyperbrains.hms.service.dto.ShiftDTO;
import com.hyperbrains.hms.service.mapper.ShiftMapper;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.hyperbrains.hms.domain.Shift}.
 *
 * <p>The one rule in this class is about the author. A shift records who wrote it, and that is taken from the
 * authenticated caller rather than from the request: a caller that supplies its own author can supply somebody
 * else's name, which is the one thing a roster of who decided who works when has to be able to rely on. An edit
 * re-writes the shift and leaves the author alone, so a later change cannot quietly take credit for an earlier one.
 *
 * <p>Everything else about a shift is left free here for now, deliberately. What a shift may be moved to, and
 * whether a person may hold two overlapping shifts, are the roster's own rules and belong with the roster's own
 * operations; the generated writes are Super Admin only until those exist, which is the same escape-hatch shape
 * every other status-bearing entity in this system uses.
 */
@Service
@Transactional
public class ShiftServiceImpl implements ShiftService {

    private static final Logger LOG = LoggerFactory.getLogger(ShiftServiceImpl.class);

    private final ShiftRepository shiftRepository;

    private final ShiftMapper shiftMapper;

    private final UserRepository userRepository;

    public ShiftServiceImpl(ShiftRepository shiftRepository, ShiftMapper shiftMapper, UserRepository userRepository) {
        this.shiftRepository = shiftRepository;
        this.shiftMapper = shiftMapper;
        this.userRepository = userRepository;
    }

    @Override
    public ShiftDTO save(ShiftDTO shiftDTO) {
        LOG.debug("Request to save Shift : {}", shiftDTO);
        Shift shift = shiftMapper.toEntity(shiftDTO);
        shift.setCreatedBy(currentUser());
        shift = shiftRepository.save(shift);
        return shiftMapper.toDto(shift);
    }

    @Override
    public ShiftDTO update(ShiftDTO shiftDTO) {
        LOG.debug("Request to update Shift : {}", shiftDTO);
        Shift existing = requireStored(shiftDTO.getId());
        Shift shift = shiftMapper.toEntity(shiftDTO);
        // An edit rewrites the shift, not its author. Who wrote the roster is part of what the row records.
        shift.setCreatedBy(existing.getCreatedBy());
        shift = shiftRepository.save(shift);
        return shiftMapper.toDto(shift);
    }

    @Override
    public Optional<ShiftDTO> partialUpdate(ShiftDTO shiftDTO) {
        LOG.debug("Request to partially update Shift : {}", shiftDTO);

        return shiftRepository
            .findById(shiftDTO.getId())
            .map(existingShift -> {
                // Held before the merge and put back after it, because a partial update carries only what is
                // changing and the author is never one of those things.
                User author = existingShift.getCreatedBy();
                shiftMapper.partialUpdate(existingShift, shiftDTO);
                existingShift.setCreatedBy(author);

                return existingShift;
            })
            .map(shiftRepository::save)
            .map(shiftMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftDTO> findAll() {
        LOG.debug("Request to get all Shifts");
        return shiftRepository.findAll().stream().map(shiftMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ShiftDTO> findOne(Long id) {
        LOG.debug("Request to get Shift : {}", id);
        return shiftRepository.findById(id).map(shiftMapper::toDto);
    }

    private Shift requireStored(Long id) {
        return shiftRepository
            .findById(id)
            .orElseThrow(() -> BusinessRuleViolationException.of("shiftNotFound", "shift", "No shift with id " + id));
    }

    private User currentUser() {
        String login = SecurityUtils
            .getCurrentUserLogin()
            .orElseThrow(() -> BusinessRuleViolationException.of("authenticationRequired", "shift", "No authenticated user in scope"));
        return userRepository
            .findOneByLogin(login)
            .orElseThrow(() -> BusinessRuleViolationException.of("unknownUser", "shift", "No user account for " + login));
    }
}
