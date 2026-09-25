package com.hyperbrains.hms.service.mapper;

import com.hyperbrains.hms.domain.AuditLog;
import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.service.dto.AuditLogDTO;
import com.hyperbrains.hms.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link AuditLog} and its DTO {@link AuditLogDTO}.
 */
@Mapper(componentModel = "spring")
public interface AuditLogMapper extends EntityMapper<AuditLogDTO, AuditLog> {
    @Mapping(target = "actor", source = "actor", qualifiedByName = "userLogin")
    AuditLogDTO toDto(AuditLog s);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);
}
