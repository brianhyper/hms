package com.hyperbrains.hms.service.mapper;

import com.hyperbrains.hms.domain.Consultation;
import com.hyperbrains.hms.domain.Diagnosis;
import com.hyperbrains.hms.service.dto.ConsultationDTO;
import com.hyperbrains.hms.service.dto.DiagnosisDTO;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Diagnosis} and its DTO {@link DiagnosisDTO}.
 */
@Mapper(componentModel = "spring")
public interface DiagnosisMapper extends EntityMapper<DiagnosisDTO, Diagnosis> {
    @Mapping(target = "consultationses", source = "consultationses", qualifiedByName = "consultationIdSet")
    DiagnosisDTO toDto(Diagnosis s);

    @Mapping(target = "consultationses", ignore = true)
    @Mapping(target = "removeConsultations", ignore = true)
    Diagnosis toEntity(DiagnosisDTO diagnosisDTO);

    @Named("consultationId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    ConsultationDTO toDtoConsultationId(Consultation consultation);

    @Named("consultationIdSet")
    default Set<ConsultationDTO> toDtoConsultationIdSet(Set<Consultation> consultation) {
        return consultation.stream().map(this::toDtoConsultationId).collect(Collectors.toSet());
    }
}
