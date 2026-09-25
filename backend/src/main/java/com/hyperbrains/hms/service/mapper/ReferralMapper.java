package com.hyperbrains.hms.service.mapper;

import com.hyperbrains.hms.domain.Department;
import com.hyperbrains.hms.domain.Referral;
import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.domain.Visit;
import com.hyperbrains.hms.service.dto.DepartmentDTO;
import com.hyperbrains.hms.service.dto.ReferralDTO;
import com.hyperbrains.hms.service.dto.UserDTO;
import com.hyperbrains.hms.service.dto.VisitDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Referral} and its DTO {@link ReferralDTO}.
 */
@Mapper(componentModel = "spring")
public interface ReferralMapper extends EntityMapper<ReferralDTO, Referral> {
    @Mapping(target = "visit", source = "visit", qualifiedByName = "visitId")
    @Mapping(target = "referredBy", source = "referredBy", qualifiedByName = "userLogin")
    @Mapping(target = "department", source = "department", qualifiedByName = "departmentId")
    ReferralDTO toDto(Referral s);

    @Named("visitId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    VisitDTO toDtoVisitId(Visit visit);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);

    @Named("departmentId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    DepartmentDTO toDtoDepartmentId(Department department);
}
