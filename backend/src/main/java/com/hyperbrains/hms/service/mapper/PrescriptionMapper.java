package com.hyperbrains.hms.service.mapper;

import com.hyperbrains.hms.domain.Prescription;
import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.domain.Visit;
import com.hyperbrains.hms.service.dto.PrescriptionDTO;
import com.hyperbrains.hms.service.dto.UserDTO;
import com.hyperbrains.hms.service.dto.VisitDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Prescription} and its DTO {@link PrescriptionDTO}.
 */
@Mapper(componentModel = "spring")
public interface PrescriptionMapper extends EntityMapper<PrescriptionDTO, Prescription> {
    @Mapping(target = "visit", source = "visit", qualifiedByName = "visitId")
    @Mapping(target = "doctor", source = "doctor", qualifiedByName = "userLogin")
    PrescriptionDTO toDto(Prescription s);

    @Named("visitId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    VisitDTO toDtoVisitId(Visit visit);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);
}
