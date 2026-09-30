package com.hyperbrains.hms.service.impl;

import com.hyperbrains.hms.domain.Bed;
import com.hyperbrains.hms.domain.BedType;
import com.hyperbrains.hms.domain.Ward;
import com.hyperbrains.hms.repository.BedRepository;
import com.hyperbrains.hms.service.BedService;
import com.hyperbrains.hms.service.BusinessRuleViolationException;
import com.hyperbrains.hms.service.dto.BedDTO;
import com.hyperbrains.hms.service.mapper.BedMapper;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.hyperbrains.hms.domain.Bed}.
 */
@Service
@Transactional
public class BedServiceImpl implements BedService {

    private static final Logger LOG = LoggerFactory.getLogger(BedServiceImpl.class);

    private final BedRepository bedRepository;

    private final BedMapper bedMapper;

    public BedServiceImpl(BedRepository bedRepository, BedMapper bedMapper) {
        this.bedRepository = bedRepository;
        this.bedMapper = bedMapper;
    }

    /**
     * Allowed: a bed is stock the hospital owns, and describing a new one is not an action on a patient.
     *
     * <p>The guard below is about what has <em>happened</em> to a bed and what it costs, so it applies to
     * changes. A new bed's first status is not a record of anything — a bed registered as needing maintenance is
     * a true statement about a bed nobody has used yet — and a wrong first status is recoverable through the
     * ward's own action, because that action asks whether an admission holds the bed rather than believing the
     * column.
     */
    @Override
    public BedDTO save(BedDTO bedDTO) {
        LOG.debug("Request to save Bed : {}", bedDTO);
        Bed bed = bedMapper.toEntity(bedDTO);
        bed = bedRepository.save(bed);
        return bedMapper.toDto(bed);
    }

    @Override
    public BedDTO update(BedDTO bedDTO) {
        LOG.debug("Request to update Bed : {}", bedDTO);
        // A PUT carries the whole record, so an empty field here means "clear it" rather than "leave it".
        refuseWorkflowOwnedChanges(bedDTO, requireStored(bedDTO.getId()), false);
        Bed bed = bedMapper.toEntity(bedDTO);
        bed = bedRepository.save(bed);
        return bedMapper.toDto(bed);
    }

    @Override
    public Optional<BedDTO> partialUpdate(BedDTO bedDTO) {
        LOG.debug("Request to partially update Bed : {}", bedDTO);

        return bedRepository
            .findById(bedDTO.getId())
            .map(existingBed -> {
                // A PATCH carries only what is changing, so an empty field here means "leave it".
                refuseWorkflowOwnedChanges(bedDTO, existingBed, true);
                bedMapper.partialUpdate(existingBed, bedDTO);

                return existingBed;
            })
            .map(bedRepository::save)
            .map(bedMapper::toDto);
    }

    /**
     * Refuses an update that would rewrite where a bed is, whether it is free, or what it costs.
     *
     * <p>The status is decided by the ward's own action, which refuses to free a bed that holds a patient and
     * refuses to empty a bed into maintenance while somebody is in it. The ward and the bed type decide who may
     * be put there and what the stay is charged at, and the override is the rate itself. All four have a guard
     * behind the route that owns them; a hand-written update to the same columns is those guards made optional,
     * which is the whole reason this refuses. The bed number is free text nobody's rules depend on.
     *
     * <p>{@code nullMeansUnchanged} is the difference between the two routes, exactly as it is for a stay: a PUT
     * that clears the ward would move the bed out of it, a PATCH that says nothing about the ward would not.
     */
    private void refuseWorkflowOwnedChanges(BedDTO requested, Bed stored, boolean nullMeansUnchanged) {
        Bed proposed = bedMapper.toEntity(requested);
        List<String> changed = new ArrayList<>();

        if (differs(proposed.getStatus(), stored.getStatus(), nullMeansUnchanged)) {
            changed.add("status");
        }
        if (differs(idOf(proposed.getWard()), idOf(stored.getWard()), nullMeansUnchanged)) {
            changed.add("ward");
        }
        if (differs(idOf(proposed.getBedType()), idOf(stored.getBedType()), nullMeansUnchanged)) {
            changed.add("bedType");
        }
        if (differs(proposed.getDailyRateOverride(), stored.getDailyRateOverride(), nullMeansUnchanged)) {
            changed.add("dailyRateOverride");
        }

        if (!changed.isEmpty()) {
            throw BusinessRuleViolationException.of(
                "bedNotEditedByHand",
                "bed",
                "A bed is moved, freed or re-priced by the ward's own actions, not by hand; this request would change " +
                String.join(", ", changed)
            );
        }
    }

    private Bed requireStored(Long id) {
        return bedRepository
            .findById(id)
            .orElseThrow(() -> BusinessRuleViolationException.of("bedNotFound", "bed", "No bed with id " + id));
    }

    private static boolean differs(Object requested, Object stored, boolean nullMeansUnchanged) {
        if (requested == null && nullMeansUnchanged) {
            return false;
        }
        return !Objects.equals(requested, stored);
    }

    /** A rate is the same rate whatever the scale says: 8000.0 and 8000.00 are not a change. */
    private static boolean differs(BigDecimal requested, BigDecimal stored, boolean nullMeansUnchanged) {
        if (requested == null && nullMeansUnchanged) {
            return false;
        }
        if (requested == null || stored == null) {
            return requested != stored;
        }
        return requested.compareTo(stored) != 0;
    }

    private static Long idOf(Ward ward) {
        return ward == null ? null : ward.getId();
    }

    private static Long idOf(BedType bedType) {
        return bedType == null ? null : bedType.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BedDTO> findAll() {
        LOG.debug("Request to get all Beds");
        return bedRepository.findAll().stream().map(bedMapper::toDto).collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BedDTO> findOne(Long id) {
        LOG.debug("Request to get Bed : {}", id);
        return bedRepository.findById(id).map(bedMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Bed : {}", id);
        bedRepository.deleteById(id);
    }
}
