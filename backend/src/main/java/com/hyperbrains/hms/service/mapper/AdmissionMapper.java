package com.hyperbrains.hms.service.mapper;

import com.hyperbrains.hms.domain.Admission;
import com.hyperbrains.hms.domain.Bed;
import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.domain.Visit;
import com.hyperbrains.hms.service.dto.AdmissionDTO;
import com.hyperbrains.hms.service.dto.BedDTO;
import com.hyperbrains.hms.service.dto.UserDTO;
import com.hyperbrains.hms.service.dto.VisitDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Admission} and its DTO {@link AdmissionDTO}.
 */
@Mapper(componentModel = "spring")
public interface AdmissionMapper extends EntityMapper<AdmissionDTO, Admission> {
    @Mapping(target = "visit", source = "visit", qualifiedByName = "visitId")
    @Mapping(target = "bed", source = "bed", qualifiedByName = "bedId")
    @Mapping(target = "admittingDoctor", source = "admittingDoctor", qualifiedByName = "userLogin")
    @Mapping(target = "primaryDoctor", source = "primaryDoctor", qualifiedByName = "userLogin")
    @Mapping(target = "dischargedByDoctor", source = "dischargedByDoctor", qualifiedByName = "userLogin")
    @Mapping(target = "dischargedByNurse", source = "dischargedByNurse", qualifiedByName = "userLogin")
    AdmissionDTO toDto(Admission s);

    @Named("visitId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    VisitDTO toDtoVisitId(Visit visit);

    @Named("bedId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    BedDTO toDtoBedId(Bed bed);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);
}
