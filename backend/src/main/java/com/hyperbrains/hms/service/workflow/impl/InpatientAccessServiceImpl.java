package com.hyperbrains.hms.service.workflow.impl;

import com.hyperbrains.hms.domain.Admission;
import com.hyperbrains.hms.domain.Patient;
import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.domain.Visit;
import com.hyperbrains.hms.domain.Ward;
import com.hyperbrains.hms.domain.WardCover;
import com.hyperbrains.hms.domain.enumeration.AdmissionStatus;
import com.hyperbrains.hms.repository.AdmissionRepository;
import com.hyperbrains.hms.repository.UserRepository;
import com.hyperbrains.hms.repository.WardCoverRepository;
import com.hyperbrains.hms.security.AuthoritiesConstants;
import com.hyperbrains.hms.security.SecurityUtils;
import com.hyperbrains.hms.service.BusinessRuleViolationException;
import com.hyperbrains.hms.service.PersonNames;
import com.hyperbrains.hms.service.dto.view.MyPatientViewDTO;
import com.hyperbrains.hms.service.dto.view.MyPatientViewDTO.SeenBecause;
import com.hyperbrains.hms.service.rules.WardCoverage;
import com.hyperbrains.hms.service.workflow.InpatientAccessService;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The inpatient row-level rule.
 */
@Service
@Transactional(readOnly = true)
public class InpatientAccessServiceImpl implements InpatientAccessService {

    private final AdmissionRepository admissionRepository;

    private final WardCoverRepository wardCoverRepository;

    private final UserRepository userRepository;

    public InpatientAccessServiceImpl(
        AdmissionRepository admissionRepository,
        WardCoverRepository wardCoverRepository,
        UserRepository userRepository
    ) {
        this.admissionRepository = admissionRepository;
        this.wardCoverRepository = wardCoverRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<MyPatientViewDTO> myPatients() {
        Instant now = Instant.now();

        if (hasFullAccess()) {
            return admissionRepository
                .findOpenStays(AdmissionStatus.DISCHARGED)
                .stream()
                .map(admission -> view(admission, SeenBecause.FULL_ACCESS))
                .toList();
        }

        User doctor = currentUser();

        // Insertion order is the tie-breaker, and the patients they are responsible for go in first: a
        // doctor who is both responsible for a patient and covering their ward should see that patient
        // listed as theirs, because that is the obligation that does not end when the shift does.
        Map<Long, Admission> visible = new LinkedHashMap<>();
        admissionRepository
            .findOpenStaysWherePrimaryDoctor(doctor.getId(), AdmissionStatus.DISCHARGED)
            .forEach(admission -> visible.put(admission.getId(), admission));

        Set<Long> coveredWards = coveredWardIds(doctor.getId(), now);
        if (!coveredWards.isEmpty()) {
            admissionRepository
                .findOpenStaysInWards(coveredWards, AdmissionStatus.DISCHARGED)
                .forEach(admission -> visible.putIfAbsent(admission.getId(), admission));
        }

        return visible
            .values()
            .stream()
            .map(admission -> view(admission, isPrimaryDoctor(admission, doctor) ? SeenBecause.PRIMARY_DOCTOR : SeenBecause.COVERING_WARD))
            .sorted((left, right) -> left.admittedAt().compareTo(right.admittedAt()))
            .toList();
    }

    @Override
    public void requireMayView(Long admissionId) {
        Admission admission = admissionRepository
            .findOneWithBedWardAndPrimaryDoctor(admissionId)
            .orElseThrow(() ->
                BusinessRuleViolationException.of("admissionNotFound", "admission", "No admission with id " + admissionId)
            );

        if (hasFullAccess()) {
            return;
        }

        User doctor = currentUser();
        if (isPrimaryDoctor(admission, doctor)) {
            return;
        }

        Ward ward = wardOf(admission);
        if (ward != null && coveredWardIds(doctor.getId(), Instant.now()).contains(ward.getId())) {
            return;
        }

        // Spring Security's own exception, which the existing translator already maps to a 403 with the stock
        // message: nothing about the request is wrong, the caller simply has no claim on this row. Throwing it
        // rather than inventing a 409 keeps that distinction visible to the client.
        throw new AccessDeniedException(
            "You are not " +
            admissionId +
            "'s responsible doctor and you are not covering " +
            (ward == null ? "the ward they are in" : ward.getName())
        );
    }

    /** The wards this doctor is covering at this moment, decided by the shared rule rather than by a query. */
    private Set<Long> coveredWardIds(Long doctorId, Instant now) {
        return wardCoverRepository
            .findByDoctorIdWithWard(doctorId)
            .stream()
            .filter(cover -> isCoverInForce(cover, now))
            .map(WardCover::getWard)
            .filter(ward -> ward != null && ward.getId() != null)
            .map(Ward::getId)
            .collect(Collectors.toSet());
    }

    /**
     * Whether this roster entry is cover the doctor can act on now. The ward being open is part of it: the
     * assignment-time check cannot help here, because the ward was open when the cover was written and nobody
     * edits the roster when a ward is closed.
     */
    private static boolean isCoverInForce(WardCover cover, Instant now) {
        Ward ward = cover.getWard();
        boolean wardTakesPatients = ward != null && Boolean.TRUE.equals(ward.getActive());
        return WardCoverage.isInForce(cover.getCoversFrom(), cover.getCoversTo(), now, wardTakesPatients);
    }

    private static boolean isPrimaryDoctor(Admission admission, User user) {
        return admission.getPrimaryDoctor() != null && user.getId().equals(admission.getPrimaryDoctor().getId());
    }

    private static Ward wardOf(Admission admission) {
        return admission.getBed() == null ? null : admission.getBed().getWard();
    }

    private static boolean hasFullAccess() {
        return SecurityUtils.hasCurrentUserAnyOfAuthorities(
            AuthoritiesConstants.NURSE,
            AuthoritiesConstants.ADMIN,
            AuthoritiesConstants.SUPER_ADMIN
        );
    }

    private MyPatientViewDTO view(Admission admission, SeenBecause seenBecause) {
        Visit visit = admission.getVisit();
        Patient patient = visit == null ? null : visit.getPatient();
        Ward ward = wardOf(admission);
        return new MyPatientViewDTO(
            admission.getId(),
            visit == null ? null : visit.getId(),
            patient == null ? null : patient.getId(),
            patient == null ? null : patient.getHospitalId(),
            patient == null ? null : patient.getFullName(),
            patient == null ? null : patient.getSex(),
            admission.getStatus(),
            admission.getBed() == null ? null : admission.getBed().getId(),
            admission.getBed() == null ? null : admission.getBed().getBedNumber(),
            ward == null ? null : ward.getId(),
            ward == null ? null : ward.getName(),
            admission.getAdmittedAt(),
            PersonNames.displayName(admission.getPrimaryDoctor()),
            seenBecause
        );
    }

    private User currentUser() {
        String login = SecurityUtils
            .getCurrentUserLogin()
            .orElseThrow(() ->
                BusinessRuleViolationException.of("authenticationRequired", "admission", "No authenticated user in scope")
            );
        return userRepository
            .findOneByLogin(login)
            .orElseThrow(() -> BusinessRuleViolationException.of("unknownUser", "admission", "No user account for " + login));
    }
}
