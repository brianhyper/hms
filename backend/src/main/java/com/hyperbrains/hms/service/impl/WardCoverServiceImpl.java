package com.hyperbrains.hms.service.impl;

import com.hyperbrains.hms.domain.WardCover;
import com.hyperbrains.hms.repository.WardCoverRepository;
import com.hyperbrains.hms.service.BusinessRuleViolationException;
import com.hyperbrains.hms.service.WardCoverService;
import com.hyperbrains.hms.service.dto.WardCoverDTO;
import com.hyperbrains.hms.service.mapper.WardCoverMapper;
import com.hyperbrains.hms.service.rules.WorkflowOwnedFields;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.hyperbrains.hms.domain.WardCover}.
 */
@Service
@Transactional
public class WardCoverServiceImpl implements WardCoverService {

    private static final Logger LOG = LoggerFactory.getLogger(WardCoverServiceImpl.class);

    private final WardCoverRepository wardCoverRepository;

    private final WardCoverMapper wardCoverMapper;

    public WardCoverServiceImpl(WardCoverRepository wardCoverRepository, WardCoverMapper wardCoverMapper) {
        this.wardCoverRepository = wardCoverRepository;
        this.wardCoverMapper = wardCoverMapper;
    }

    /**
     * When the cover applies, which the cover operations own.
     *
     * <p>{@code coversFrom} and {@code coversTo} are what decides whether a ward was covered at a given moment, and
     * that is the question asked afterwards — during a review, or after something went wrong on a shift. Moving the
     * period by hand moves the answer, and it does it without the record of a cover being started or ended, which is
     * the only thing that ever said who was responsible for the ward.
     *
     * <p>The people and the ward on the cover are guarded too, but separately and by id rather than by field name: a
     * reference cannot go in this list, because the stored one is a proxy and this guard compares by identity. See
     * {@link WorkflowOwnedFields#referenceChanged}.
     */
    private static final String[] WORKFLOW_OWNED_FIELDS = { "coversFrom", "coversTo" };

    @Override
    public WardCoverDTO save(WardCoverDTO wardCoverDTO) {
        LOG.debug("Request to save WardCover : {}", wardCoverDTO);
        WardCover wardCover = wardCoverMapper.toEntity(wardCoverDTO);
        wardCover = wardCoverRepository.save(wardCover);
        return wardCoverMapper.toDto(wardCover);
    }

    @Override
    public WardCoverDTO update(WardCoverDTO wardCoverDTO) {
        LOG.debug("Request to update WardCover : {}", wardCoverDTO);
        // A PUT carries the whole record, so an empty field here means "clear it" rather than "leave it".
        refuseHandEdits(wardCoverDTO, requireStored(wardCoverDTO.getId()), false);
        WardCover wardCover = wardCoverMapper.toEntity(wardCoverDTO);
        wardCover = wardCoverRepository.save(wardCover);
        return wardCoverMapper.toDto(wardCover);
    }

    @Override
    public Optional<WardCoverDTO> partialUpdate(WardCoverDTO wardCoverDTO) {
        LOG.debug("Request to partially update WardCover : {}", wardCoverDTO);

        return wardCoverRepository
            .findById(wardCoverDTO.getId())
            .map(existingWardCover -> {
                // A PATCH carries only what is changing, so an empty field here means "leave it".
                refuseHandEdits(wardCoverDTO, existingWardCover, true);
                wardCoverMapper.partialUpdate(existingWardCover, wardCoverDTO);

                return existingWardCover;
            })
            .map(wardCoverRepository::save)
            .map(wardCoverMapper::toDto);
    }

    private void refuseHandEdits(WardCoverDTO requested, WardCover stored, boolean nullMeansUnchanged) {
        List<String> changed = new ArrayList<>(
            WorkflowOwnedFields.changed(wardCoverMapper.toEntity(requested), stored, nullMeansUnchanged, WORKFLOW_OWNED_FIELDS)
        );

        // The ward and the two people are compared by id, not as entities: the stored side is a Hibernate proxy and
        // reading an id off it forces a load. The request carries the ids as plain values and the stored ids come from
        // a scalar projection, so neither side touches a proxy and a legitimate edit that re-sends the same ward is not
        // mistaken for a re-point.
        WardCoverRepository.ReferenceIds storedIds = wardCoverRepository
            .findReferenceIds(stored.getId())
            .orElseThrow(() ->
                BusinessRuleViolationException.of("wardCoverNotFound", "wardCover", "No ward cover with id " + stored.getId())
            );
        addIfChanged(changed, "ward", idOfWard(requested), storedIds.getWardId(), nullMeansUnchanged);
        addIfChanged(changed, "doctor", idOfDoctor(requested), storedIds.getDoctorId(), nullMeansUnchanged);
        addIfChanged(changed, "assignedBy", idOfAssignedBy(requested), storedIds.getAssignedById(), nullMeansUnchanged);

        if (!changed.isEmpty()) {
            throw BusinessRuleViolationException.of(
                "wardCoverNotEditedByHand",
                "wardCover",
                "A cover is started and ended by its own operations, which record who decided; this request would change " +
                String.join(", ", changed)
            );
        }
    }

    private static void addIfChanged(List<String> changed, String field, Long requestedId, Long storedId, boolean nullMeansUnchanged) {
        if (WorkflowOwnedFields.referenceChanged(requestedId, storedId, nullMeansUnchanged)) {
            changed.add(field);
        }
    }

    private static Long idOfWard(WardCoverDTO requested) {
        return requested.getWard() == null ? null : requested.getWard().getId();
    }

    private static Long idOfDoctor(WardCoverDTO requested) {
        return requested.getDoctor() == null ? null : requested.getDoctor().getId();
    }

    private static Long idOfAssignedBy(WardCoverDTO requested) {
        return requested.getAssignedBy() == null ? null : requested.getAssignedBy().getId();
    }

    private WardCover requireStored(Long id) {
        return wardCoverRepository
            .findById(id)
            .orElseThrow(() -> BusinessRuleViolationException.of("wardCoverNotFound", "wardCover", "No ward cover with id " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<WardCoverDTO> findAll() {
        LOG.debug("Request to get all WardCovers");
        return wardCoverRepository.findAll().stream().map(wardCoverMapper::toDto).collect(Collectors.toCollection(LinkedList::new));
    }

    public Page<WardCoverDTO> findAllWithEagerRelationships(Pageable pageable) {
        return wardCoverRepository.findAllWithEagerRelationships(pageable).map(wardCoverMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<WardCoverDTO> findOne(Long id) {
        LOG.debug("Request to get WardCover : {}", id);
        return wardCoverRepository.findOneWithEagerRelationships(id).map(wardCoverMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete WardCover : {}", id);
        wardCoverRepository.deleteById(id);
    }
}
