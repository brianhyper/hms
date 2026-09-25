package com.hyperbrains.hms.service.mapper;

import com.hyperbrains.hms.domain.Patient;
import com.hyperbrains.hms.service.dto.PatientDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Patient} and its DTO {@link PatientDTO}.
 */
@Mapper(componentModel = "spring")
public interface PatientMapper extends EntityMapper<PatientDTO, Patient> {
    @Mapping(target = "mergedIntoPatient", source = "mergedIntoPatient", qualifiedByName = "patientId")
    PatientDTO toDto(Patient s);

    @Named("patientId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    PatientDTO toDtoPatientId(Patient patient);
}
