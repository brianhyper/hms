package com.hyperbrains.hms.service.workflow.impl;

import com.hyperbrains.hms.domain.Admission;
import com.hyperbrains.hms.domain.Patient;
import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.domain.Visit;
import com.hyperbrains.hms.domain.enumeration.AdmissionStatus;
import com.hyperbrains.hms.repository.AdmissionRepository;
import com.hyperbrains.hms.service.dto.view.AwaitingBedViewDTO;
import com.hyperbrains.hms.service.workflow.InpatientWorklistService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The inpatient worklists, read-only.
 */
@Service
@Transactional(readOnly = true)
public class InpatientWorklistServiceImpl implements InpatientWorklistService {

    private final AdmissionRepository admissionRepository;

    public InpatientWorklistServiceImpl(AdmissionRepository admissionRepository) {
        this.admissionRepository = admissionRepository;
    }

    @Override
    public List<AwaitingBedViewDTO> awaitingBed() {
        return admissionRepository
            .findAwaitingBed(AdmissionStatus.PENDING_BED)
            .stream()
            .map(InpatientWorklistServiceImpl::toView)
            .toList();
    }

    private static AwaitingBedViewDTO toView(Admission admission) {
        Visit visit = admission.getVisit();
        Patient patient = visit == null ? null : visit.getPatient();
        return new AwaitingBedViewDTO(
            admission.getId(),
            visit == null ? null : visit.getId(),
            patient == null ? null : patient.getId(),
            patient == null ? null : patient.getHospitalId(),
            patient == null ? null : patient.getFullName(),
            patient == null ? null : patient.getSex(),
            patient == null ? null : patient.getDateOfBirth(),
            patient == null ? null : patient.getEstimatedAge(),
            admission.getAdmittedAt(),
            admission.getAdmissionReason(),
            displayName(admission.getAdmittingDoctor()),
            displayName(admission.getPrimaryDoctor())
        );
    }

    /** A name to work from, falling back to the login when the account has no names recorded. */
    private static String displayName(User user) {
        if (user == null) {
            return null;
        }
        String first = user.getFirstName() == null ? "" : user.getFirstName();
        String last = user.getLastName() == null ? "" : user.getLastName();
        String full = (first + " " + last).trim();
        return full.isEmpty() ? user.getLogin() : full;
    }
}
