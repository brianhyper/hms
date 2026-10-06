package com.hyperbrains.hms.service.impl;

import com.hyperbrains.hms.config.HmsProperties;
import com.hyperbrains.hms.domain.Shift;
import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.repository.ShiftRepository;
import com.hyperbrains.hms.repository.UserRepository;
import com.hyperbrains.hms.security.SecurityUtils;
import com.hyperbrains.hms.service.BusinessRuleViolationException;
import com.hyperbrains.hms.service.ShiftService;
import com.hyperbrains.hms.service.dto.ShiftDTO;
import com.hyperbrains.hms.service.dto.view.ShiftViewDTO;
import com.hyperbrains.hms.service.mapper.ShiftMapper;
import com.hyperbrains.hms.service.rules.ShiftDuty;
import java.time.LocalDateTime;
import java.time.ZoneId;
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

    private final HmsProperties properties;

    public ShiftServiceImpl(
        ShiftRepository shiftRepository,
        ShiftMapper shiftMapper,
        UserRepository userRepository,
        HmsProperties properties
    ) {
        this.shiftRepository = shiftRepository;
        this.shiftMapper = shiftMapper;
        this.userRepository = userRepository;
        this.properties = properties;
    }

    @Override
    public ShiftDTO save(ShiftDTO shiftDTO) {
        LOG.debug("Request to save Shift : {}", shiftDTO);
        Shift shift = shiftMapper.toEntity(shiftDTO);
        refuseAShiftThatIsNotAWindow(shift);
        shift.setCreatedBy(currentUser());
        shift = shiftRepository.save(shift);
        return shiftMapper.toDto(shift);
    }

    @Override
    public ShiftDTO update(ShiftDTO shiftDTO) {
        LOG.debug("Request to update Shift : {}", shiftDTO);
        Shift existing = requireStored(shiftDTO.getId());
        Shift shift = shiftMapper.toEntity(shiftDTO);
        refuseAShiftThatIsNotAWindow(shift);
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
                // Checked on the merged row rather than on the request: a patch that sends one of the two times is
                // enough to make the window impossible, and what is stored is what is read.
                refuseAShiftThatIsNotAWindow(existingShift);

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

    @Override
    @Transactional(readOnly = true)
    public List<ShiftViewDTO> onDutyNow() {
        return onDutyAt(LocalDateTime.now(hospitalZone()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftViewDTO> onDutyNowInWard(Long wardId) {
        return onDutyAt(LocalDateTime.now(hospitalZone())).stream().filter(shift -> wardId.equals(shift.wardId())).toList();
    }

    /**
     * Who is on duty at a moment, decided by the shared rule rather than by a query.
     *
     * <p>The query fetches candidates for today and yesterday and this filters them, which is the same division of
     * labour the ward-cover rule used: the window comparison lives in one place, {@link ShiftDuty}, where it is
     * testable to the minute, rather than being restated in JPQL where it would be a second copy that the tests do
     * not reach. Yesterday is fetched for the night shift — at 02:00 the person on duty is the one whose shift was
     * written for the day before.
     */
    private List<ShiftViewDTO> onDutyAt(LocalDateTime at) {
        return shiftRepository
            .findWithPeopleOnDates(ShiftDuty.candidateDates(at))
            .stream()
            .filter(shift -> ShiftDuty.isOnDutyAt(shift.getShiftDate(), shift.getStartsAt(), shift.getEndsAt(), at))
            .map(ShiftViewDTO::from)
            .toList();
    }

    /**
     * The hospital's own wall clock, read from the one place it is configured.
     *
     * <p>It matters here and not in the table: a roster says "07:00 to 19:00 on the eighth", and whether that covers
     * the moment the question is asked is a question about the hospital's clock, not about UTC. Reading the zone from
     * configuration rather than assuming one is the same choice the appointment reminders already make.
     */
    private ZoneId hospitalZone() {
        return ZoneId.of(properties.getAppointments().getZone());
    }

    /**
     * A shift has to be a window.
     *
     * <p>A shift that ends when it starts could mean nothing or the whole day, and the roster would then hold a row
     * that no reader can answer for: {@code ShiftDuty} answers "not on duty" for it, so it would look like cover and
     * grant nothing — the failure mode that made a period of ward cover refuse equal end times too.
     */
    private void refuseAShiftThatIsNotAWindow(Shift shift) {
        if (ShiftDuty.isWellFormed(shift.getStartsAt(), shift.getEndsAt())) {
            return;
        }
        throw BusinessRuleViolationException.of(
            "shiftNotAWindow",
            "shift",
            (
                "A shift runs from a start time to a different end time; " +
                shift.getStartsAt() +
                " to " +
                shift.getEndsAt() +
                " covers no time. A 24-hour duty is two shifts, day and night."
            )
        );
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
