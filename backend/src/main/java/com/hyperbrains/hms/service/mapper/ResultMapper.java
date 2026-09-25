package com.hyperbrains.hms.service.mapper;

import com.hyperbrains.hms.domain.Result;
import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.service.dto.ResultDTO;
import com.hyperbrains.hms.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Result} and its DTO {@link ResultDTO}.
 */
@Mapper(componentModel = "spring")
public interface ResultMapper extends EntityMapper<ResultDTO, Result> {
    @Mapping(target = "enteredBy", source = "enteredBy", qualifiedByName = "userLogin")
    ResultDTO toDto(Result s);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);
}
