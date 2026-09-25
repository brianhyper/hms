package com.hyperbrains.hms.service.mapper;

import com.hyperbrains.hms.domain.Consultation;
import com.hyperbrains.hms.domain.Diagnosis;
import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.service.dto.ConsultationDTO;
import com.hyperbrains.hms.service.dto.DiagnosisDTO;
import com.hyperbrains.hms.service.dto.UserDTO;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Consultation} and its DTO {@link ConsultationDTO}.
 */
@Mapper(componentModel = "spring")
public interface ConsultationMapper extends EntityMapper<ConsultationDTO, Consultation> {
    @Mapping(target = "doctor", source = "doctor", qualifiedByName = "userLogin")
    @Mapping(target = "diagnoseses", source = "diagnoseses", qualifiedByName = "diagnosisIdSet")
    ConsultationDTO toDto(Consultation s);

    @Mapping(target = "removeDiagnoses", ignore = true)
    Consultation toEntity(ConsultationDTO consultationDTO);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);

    @Named("diagnosisId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    DiagnosisDTO toDtoDiagnosisId(Diagnosis diagnosis);

    @Named("diagnosisIdSet")
    default Set<DiagnosisDTO> toDtoDiagnosisIdSet(Set<Diagnosis> diagnosis) {
        return diagnosis.stream().map(this::toDtoDiagnosisId).collect(Collectors.toSet());
    }
}
