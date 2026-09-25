package com.hyperbrains.hms.service.mapper;

import com.hyperbrains.hms.domain.Admission;
import com.hyperbrains.hms.domain.DoctorOrder;
import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.service.dto.AdmissionDTO;
import com.hyperbrains.hms.service.dto.DoctorOrderDTO;
import com.hyperbrains.hms.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link DoctorOrder} and its DTO {@link DoctorOrderDTO}.
 */
@Mapper(componentModel = "spring")
public interface DoctorOrderMapper extends EntityMapper<DoctorOrderDTO, DoctorOrder> {
    @Mapping(target = "admission", source = "admission", qualifiedByName = "admissionId")
    @Mapping(target = "orderedBy", source = "orderedBy", qualifiedByName = "userLogin")
    @Mapping(target = "cancelledBy", source = "cancelledBy", qualifiedByName = "userLogin")
    DoctorOrderDTO toDto(DoctorOrder s);

    @Named("admissionId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    AdmissionDTO toDtoAdmissionId(Admission admission);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);
}
