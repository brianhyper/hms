package com.hyperbrains.hms.service.mapper;

import com.hyperbrains.hms.domain.Dispense;
import com.hyperbrains.hms.domain.Prescription;
import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.service.dto.DispenseDTO;
import com.hyperbrains.hms.service.dto.PrescriptionDTO;
import com.hyperbrains.hms.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Dispense} and its DTO {@link DispenseDTO}.
 */
@Mapper(componentModel = "spring")
public interface DispenseMapper extends EntityMapper<DispenseDTO, Dispense> {
    @Mapping(target = "prescription", source = "prescription", qualifiedByName = "prescriptionId")
    @Mapping(target = "recordedBy", source = "recordedBy", qualifiedByName = "userLogin")
    DispenseDTO toDto(Dispense s);

    @Named("prescriptionId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    PrescriptionDTO toDtoPrescriptionId(Prescription prescription);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);
}
