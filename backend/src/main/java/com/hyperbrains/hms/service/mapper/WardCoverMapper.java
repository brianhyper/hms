package com.hyperbrains.hms.service.mapper;

import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.domain.Ward;
import com.hyperbrains.hms.domain.WardCover;
import com.hyperbrains.hms.service.dto.UserDTO;
import com.hyperbrains.hms.service.dto.WardCoverDTO;
import com.hyperbrains.hms.service.dto.WardDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link WardCover} and its DTO {@link WardCoverDTO}.
 */
@Mapper(componentModel = "spring")
public interface WardCoverMapper extends EntityMapper<WardCoverDTO, WardCover> {
    @Mapping(target = "doctor", source = "doctor", qualifiedByName = "userLogin")
    @Mapping(target = "ward", source = "ward", qualifiedByName = "wardId")
    @Mapping(target = "assignedBy", source = "assignedBy", qualifiedByName = "userLogin")
    WardCoverDTO toDto(WardCover s);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);

    @Named("wardId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    WardDTO toDtoWardId(Ward ward);
}
