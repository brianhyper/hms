package com.hyperbrains.hms.service.impl;

import com.hyperbrains.hms.domain.VitalSigns;
import com.hyperbrains.hms.repository.VitalSignsRepository;
import com.hyperbrains.hms.service.BusinessRuleViolationException;
import com.hyperbrains.hms.service.VitalSignsService;
import com.hyperbrains.hms.service.dto.VitalSignsDTO;
import com.hyperbrains.hms.service.mapper.VitalSignsMapper;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.hyperbrains.hms.domain.VitalSigns}.
 */
@Service
@Transactional
public class VitalSignsServiceImpl implements VitalSignsService {

    private static final Logger LOG = LoggerFactory.getLogger(VitalSignsServiceImpl.class);

    private final VitalSignsRepository vitalSignsRepository;

    private final VitalSignsMapper vitalSignsMapper;

    public VitalSignsServiceImpl(VitalSignsRepository vitalSignsRepository, VitalSignsMapper vitalSignsMapper) {
        this.vitalSignsRepository = vitalSignsRepository;
        this.vitalSignsMapper = vitalSignsMapper;
    }

    /**
     * Refused: a reading is taken, not written, and a later correction supersedes it rather than replacing it.
     *
     * <p>This is the shape the rest of this system already uses for clinical records, and the reason
     * {@code PatientCorrectionResource} exists beside the generated patient update: an edit with no author and no
     * reason attached cannot afterwards be told apart from a reading that was always that value. Vitals are recorded
     * through triage, which checks the numbers against the warning thresholds and records who took them; a later
     * change goes through the same route as a correction, which requires a reason and keeps what the reading was.
     *
     * <p>These routes are open to NURSE and DOCTOR rather than only to a system administrator, so this was the one
     * door of its kind that the people doing the clinical work could have walked through.
     */
    @Override
    public VitalSignsDTO save(VitalSignsDTO vitalSignsDTO) {
        LOG.debug("Request to save VitalSigns : {}", vitalSignsDTO);
        throw BusinessRuleViolationException.of(
            "vitalsNotRecordedByHand",
            "vitalSigns",
            "Vitals are recorded through triage, which checks the reading and records who took it; use POST /api/visit-triage/{visitId}/vitals"
        );
    }

    @Override
    public VitalSignsDTO update(VitalSignsDTO vitalSignsDTO) {
        LOG.debug("Request to update VitalSigns : {}", vitalSignsDTO);
        throw refusedBecauseACorrectionIsNotAnEdit();
    }

    @Override
    public Optional<VitalSignsDTO> partialUpdate(VitalSignsDTO vitalSignsDTO) {
        LOG.debug("Request to partially update VitalSigns : {}", vitalSignsDTO);
        throw refusedBecauseACorrectionIsNotAnEdit();
    }

    private static BusinessRuleViolationException refusedBecauseACorrectionIsNotAnEdit() {
        return BusinessRuleViolationException.of(
            "vitalSignsNotEditedByHand",
            "vitalSigns",
            "Vitals are not edited: a correction is recorded through triage, which requires a reason and keeps what the reading was; use POST /api/visit-triage/{visitId}/vitals"
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<VitalSignsDTO> findAll() {
        LOG.debug("Request to get all VitalSignses");
        return vitalSignsRepository.findAll().stream().map(vitalSignsMapper::toDto).collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     *  Get all the vitalSignses where Visit is {@code null}.
     *  @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<VitalSignsDTO> findAllWhereVisitIsNull() {
        LOG.debug("Request to get all vitalSignses where Visit is null");
        return StreamSupport.stream(vitalSignsRepository.findAll().spliterator(), false)
            .filter(vitalSigns -> vitalSigns.getVisit() == null)
            .map(vitalSignsMapper::toDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<VitalSignsDTO> findOne(Long id) {
        LOG.debug("Request to get VitalSigns : {}", id);
        return vitalSignsRepository.findById(id).map(vitalSignsMapper::toDto);
    }

    /**
     * Refused: a reading that was taken stays taken, even when it turns out to be wrong.
     *
     * <p>Deleting is the strongest possible edit — it removes the evidence that there was anything to disagree with,
     * and a chart that is missing a reading is indistinguishable from a patient who was never observed. Wrong
     * readings are superseded by a correction, which keeps this row and points at the one that replaces it.
     */
    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete VitalSigns : {}", id);
        throw BusinessRuleViolationException.of(
            "vitalSignsNotDeleted",
            "vitalSigns",
            "A reading that was taken stays on the chart; a correction supersedes it through POST /api/visit-triage/{visitId}/vitals"
        );
    }
}
