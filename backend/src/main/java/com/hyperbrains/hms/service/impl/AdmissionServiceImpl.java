package com.hyperbrains.hms.service.impl;

import com.hyperbrains.hms.domain.Admission;
import com.hyperbrains.hms.domain.Bed;
import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.domain.Visit;
import com.hyperbrains.hms.repository.AdmissionRepository;
import com.hyperbrains.hms.service.AdmissionService;
import com.hyperbrains.hms.service.BusinessRuleViolationException;
import com.hyperbrains.hms.service.dto.AdmissionDTO;
import com.hyperbrains.hms.service.mapper.AdmissionMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.hyperbrains.hms.domain.Admission}.
 */
@Service
@Transactional
public class AdmissionServiceImpl implements AdmissionService {

    private static final Logger LOG = LoggerFactory.getLogger(AdmissionServiceImpl.class);

    private final AdmissionRepository admissionRepository;

    private final AdmissionMapper admissionMapper;

    public AdmissionServiceImpl(AdmissionRepository admissionRepository, AdmissionMapper admissionMapper) {
        this.admissionRepository = admissionRepository;
        this.admissionMapper = admissionMapper;
    }

    /**
     * Refused outright: a stay is opened by the admission action, not by a row.
     *
     * <p>{@code POST /api/visit-admissions/\{visitId\}/admit} does three things a raw insert cannot: it places
     * the patient in a bed, links the visit that the money and the clinical record hang off, and refuses when the
     * ward has already said something else about that patient. A row written here would be a stay that never went
     * through any of them, which is worse than no stay at all, because the ward would then trust it.
     */
    @Override
    public AdmissionDTO save(AdmissionDTO admissionDTO) {
        LOG.debug("Request to save Admission : {}", admissionDTO);
        throw BusinessRuleViolationException.of(
            "admissionNotCreatedByHand",
            "admission",
            "A stay begins at admission (POST /api/visit-admissions/{visitId}/admit), which places the patient in a bed and opens the record, so it cannot be created here"
        );
    }

    @Override
    public AdmissionDTO update(AdmissionDTO admissionDTO) {
        LOG.debug("Request to update Admission : {}", admissionDTO);
        // A PUT carries the whole record, so a field left empty here means "clear it" rather than "leave it".
        refuseWorkflowOwnedChanges(admissionDTO, requireStored(admissionDTO.getId()), false);
        Admission admission = admissionMapper.toEntity(admissionDTO);
        admission = admissionRepository.save(admission);
        return admissionMapper.toDto(admission);
    }

    @Override
    public Optional<AdmissionDTO> partialUpdate(AdmissionDTO admissionDTO) {
        LOG.debug("Request to partially update Admission : {}", admissionDTO);

        return admissionRepository
            .findById(admissionDTO.getId())
            .map(existingAdmission -> {
                // A PATCH carries only what is changing, so a field left empty here means "leave it".
                refuseWorkflowOwnedChanges(admissionDTO, existingAdmission, true);
                admissionMapper.partialUpdate(existingAdmission, admissionDTO);

                return existingAdmission;
            })
            .map(admissionRepository::save)
            .map(admissionMapper::toDto);
    }

    /**
     * Refuses an update that would rewrite what has happened to the patient, naming what it tried to change.
     *
     * <p>The status, the bed, who is responsible and how the stay ended are decided by the specification's own
     * actions — admit, assign a bed, transfer, discharge — and each of those does more than set a column:
     * assigning a bed marks it occupied, transferring frees the old one into cleaning, discharging closes the
     * visit and its money. A hand-written update to the same column skips every one of those, which is how a ward
     * model quietly starts disagreeing with the ward it describes. Reading them stays open; writing them is the
     * workflow's. Free text — the reason recorded at admission — is left alone, because no workflow owns it.
     *
     * <p>{@code nullMeansUnchanged} is the whole difference between the two routes. A PUT carries the whole
     * record, so clearing the bed there is a change and is refused; a PATCH says nothing about the bed when it
     * only corrects the reason, and is not.
     */
    private void refuseWorkflowOwnedChanges(AdmissionDTO requested, Admission stored, boolean nullMeansUnchanged) {
        Admission proposed = admissionMapper.toEntity(requested);
        List<String> changed = new ArrayList<>();

        if (differs(proposed.getStatus(), stored.getStatus(), nullMeansUnchanged)) {
            changed.add("status");
        }
        if (differs(idOf(proposed.getVisit()), idOf(stored.getVisit()), nullMeansUnchanged)) {
            changed.add("visit");
        }
        if (differs(idOf(proposed.getBed()), idOf(stored.getBed()), nullMeansUnchanged)) {
            changed.add("bed");
        }
        if (differs(idOf(proposed.getAdmittingDoctor()), idOf(stored.getAdmittingDoctor()), nullMeansUnchanged)) {
            changed.add("admittingDoctor");
        }
        if (differs(idOf(proposed.getPrimaryDoctor()), idOf(stored.getPrimaryDoctor()), nullMeansUnchanged)) {
            changed.add("primaryDoctor");
        }
        if (differs(proposed.getAdmittedAt(), stored.getAdmittedAt(), nullMeansUnchanged)) {
            changed.add("admittedAt");
        }
        if (differs(proposed.getDischargedAt(), stored.getDischargedAt(), nullMeansUnchanged)) {
            changed.add("dischargedAt");
        }
        if (differs(proposed.getDischargeNote(), stored.getDischargeNote(), nullMeansUnchanged)) {
            changed.add("dischargeNote");
        }
        if (differs(idOf(proposed.getDischargedByDoctor()), idOf(stored.getDischargedByDoctor()), nullMeansUnchanged)) {
            changed.add("dischargedByDoctor");
        }
        if (differs(idOf(proposed.getDischargedByNurse()), idOf(stored.getDischargedByNurse()), nullMeansUnchanged)) {
            changed.add("dischargedByNurse");
        }

        if (!changed.isEmpty()) {
            throw BusinessRuleViolationException.of(
                "admissionNotEditedByHand",
                "admission",
                "A stay is changed by the ward's own actions, not by hand; this request would change " + String.join(", ", changed)
            );
        }
    }

    private Admission requireStored(Long id) {
        return admissionRepository
            .findById(id)
            .orElseThrow(() -> BusinessRuleViolationException.of("admissionNotFound", "admission", "No admission with id " + id));
    }

    private static boolean differs(Object requested, Object stored, boolean nullMeansUnchanged) {
        if (requested == null && nullMeansUnchanged) {
            return false;
        }
        return !Objects.equals(requested, stored);
    }

    private static Long idOf(Visit visit) {
        return visit == null ? null : visit.getId();
    }

    private static Long idOf(Bed bed) {
        return bed == null ? null : bed.getId();
    }

    private static Long idOf(User user) {
        return user == null ? null : user.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdmissionDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Admissions");
        return admissionRepository.findAll(pageable).map(admissionMapper::toDto);
    }

    public Page<AdmissionDTO> findAllWithEagerRelationships(Pageable pageable) {
        return admissionRepository.findAllWithEagerRelationships(pageable).map(admissionMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AdmissionDTO> findOne(Long id) {
        LOG.debug("Request to get Admission : {}", id);
        return admissionRepository.findOneWithEagerRelationships(id).map(admissionMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Admission : {}", id);
        admissionRepository.deleteById(id);
    }
}
