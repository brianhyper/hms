package com.hyperbrains.hms.service.workflow.impl;

import com.hyperbrains.hms.domain.Bed;
import com.hyperbrains.hms.domain.Department;
import com.hyperbrains.hms.domain.Ward;
import com.hyperbrains.hms.domain.enumeration.BedStatus;
import com.hyperbrains.hms.repository.BedRepository;
import com.hyperbrains.hms.repository.WardRepository;
import com.hyperbrains.hms.service.dto.view.BedAvailabilityViewDTO;
import com.hyperbrains.hms.service.dto.view.WardOccupancyViewDTO;
import com.hyperbrains.hms.service.rules.BedPricing;
import com.hyperbrains.hms.service.workflow.BedAvailabilityService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reads over beds: which are free, and how full each ward is.
 */
@Service
@Transactional(readOnly = true)
public class BedAvailabilityServiceImpl implements BedAvailabilityService {

    private final BedRepository bedRepository;

    private final WardRepository wardRepository;

    public BedAvailabilityServiceImpl(BedRepository bedRepository, WardRepository wardRepository) {
        this.bedRepository = bedRepository;
        this.wardRepository = wardRepository;
    }

    @Override
    public List<BedAvailabilityViewDTO> findAssignable(Long wardId, Long bedTypeId) {
        return bedRepository
            .findAssignable(BedStatus.AVAILABLE, wardId, bedTypeId)
            .stream()
            .map(BedAvailabilityServiceImpl::toView)
            .toList();
    }

    @Override
    public List<WardOccupancyViewDTO> wardOccupancy() {
        Map<Long, List<Bed>> bedsByWard = bedRepository
            .findAllWithWardAndBedType()
            .stream()
            .filter(bed -> bed.getWard() != null && bed.getWard().getId() != null)
            .collect(Collectors.groupingBy(bed -> bed.getWard().getId()));

        return wardRepository
            .findAllByOrderByNameAsc()
            .stream()
            .map(ward -> toOccupancy(ward, bedsByWard.getOrDefault(ward.getId(), List.of())))
            .toList();
    }

    private static WardOccupancyViewDTO toOccupancy(Ward ward, List<Bed> beds) {
        Department department = ward.getDepartment();
        return new WardOccupancyViewDTO(
            ward.getId(),
            ward.getName(),
            department == null ? null : department.getName(),
            Boolean.TRUE.equals(ward.getActive()),
            beds.size(),
            count(beds, BedStatus.AVAILABLE),
            count(beds, BedStatus.OCCUPIED),
            count(beds, BedStatus.CLEANING),
            count(beds, BedStatus.MAINTENANCE)
        );
    }

    private static BedAvailabilityViewDTO toView(Bed bed) {
        Ward ward = bed.getWard();
        return new BedAvailabilityViewDTO(
            bed.getId(),
            bed.getBedNumber(),
            ward == null ? null : ward.getId(),
            ward == null ? null : ward.getName(),
            bed.getBedType() == null ? null : bed.getBedType().getId(),
            bed.getBedType() == null ? null : bed.getBedType().getName(),
            BedPricing.effectiveDailyRate(bed),
            BedPricing.rateComesFromTheBed(bed)
        );
    }

    private static long count(List<Bed> beds, BedStatus status) {
        return beds.stream().filter(bed -> bed.getStatus() == status).count();
    }
}
