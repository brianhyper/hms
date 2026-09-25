package com.hyperbrains.hms.service.mapper;

import com.hyperbrains.hms.domain.Payment;
import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.service.dto.PaymentDTO;
import com.hyperbrains.hms.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Payment} and its DTO {@link PaymentDTO}.
 */
@Mapper(componentModel = "spring")
public interface PaymentMapper extends EntityMapper<PaymentDTO, Payment> {
    @Mapping(target = "recordedBy", source = "recordedBy", qualifiedByName = "userLogin")
    PaymentDTO toDto(Payment s);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);
}
