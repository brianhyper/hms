package com.hyperbrains.hms.service.mapper;

import com.hyperbrains.hms.domain.AdHocCharge;
import com.hyperbrains.hms.domain.Admission;
import com.hyperbrains.hms.domain.HospitalService;
import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.service.dto.AdHocChargeDTO;
import com.hyperbrains.hms.service.dto.AdmissionDTO;
import com.hyperbrains.hms.service.dto.HospitalServiceDTO;
import com.hyperbrains.hms.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link AdHocCharge} and its DTO {@link AdHocChargeDTO}.
 */
@Mapper(componentModel = "spring")
public interface AdHocChargeMapper extends EntityMapper<AdHocChargeDTO, AdHocCharge> {
    @Mapping(target = "admission", source = "admission", qualifiedByName = "admissionId")
    @Mapping(target = "serviceCatalogue", source = "serviceCatalogue", qualifiedByName = "hospitalServiceId")
    @Mapping(target = "addedBy", source = "addedBy", qualifiedByName = "userLogin")
    @Mapping(target = "voidedBy", source = "voidedBy", qualifiedByName = "userLogin")
    AdHocChargeDTO toDto(AdHocCharge s);

    @Named("admissionId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    AdmissionDTO toDtoAdmissionId(Admission admission);

    @Named("hospitalServiceId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    HospitalServiceDTO toDtoHospitalServiceId(HospitalService hospitalService);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);
}
