package com.hyperbrains.hms.service.mapper;

import com.hyperbrains.hms.domain.Admission;
import com.hyperbrains.hms.domain.InpatientVitals;
import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.service.dto.AdmissionDTO;
import com.hyperbrains.hms.service.dto.InpatientVitalsDTO;
import com.hyperbrains.hms.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link InpatientVitals} and its DTO {@link InpatientVitalsDTO}.
 */
@Mapper(componentModel = "spring")
public interface InpatientVitalsMapper extends EntityMapper<InpatientVitalsDTO, InpatientVitals> {
    @Mapping(target = "admission", source = "admission", qualifiedByName = "admissionId")
    @Mapping(target = "recordedBy", source = "recordedBy", qualifiedByName = "userLogin")
    InpatientVitalsDTO toDto(InpatientVitals s);

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
