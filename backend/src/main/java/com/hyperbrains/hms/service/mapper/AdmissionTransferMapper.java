package com.hyperbrains.hms.service.mapper;

import com.hyperbrains.hms.domain.Admission;
import com.hyperbrains.hms.domain.AdmissionTransfer;
import com.hyperbrains.hms.domain.Bed;
import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.service.dto.AdmissionDTO;
import com.hyperbrains.hms.service.dto.AdmissionTransferDTO;
import com.hyperbrains.hms.service.dto.BedDTO;
import com.hyperbrains.hms.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link AdmissionTransfer} and its DTO {@link AdmissionTransferDTO}.
 */
@Mapper(componentModel = "spring")
public interface AdmissionTransferMapper extends EntityMapper<AdmissionTransferDTO, AdmissionTransfer> {
    @Mapping(target = "admission", source = "admission", qualifiedByName = "admissionId")
    @Mapping(target = "fromBed", source = "fromBed", qualifiedByName = "bedId")
    @Mapping(target = "toBed", source = "toBed", qualifiedByName = "bedId")
    @Mapping(target = "transferredBy", source = "transferredBy", qualifiedByName = "userLogin")
    AdmissionTransferDTO toDto(AdmissionTransfer s);

    @Named("admissionId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    AdmissionDTO toDtoAdmissionId(Admission admission);

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
