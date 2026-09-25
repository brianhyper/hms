package com.hyperbrains.hms.service.mapper;

import com.hyperbrains.hms.domain.DoctorOrder;
import com.hyperbrains.hms.domain.OrderExecution;
import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.service.dto.DoctorOrderDTO;
import com.hyperbrains.hms.service.dto.OrderExecutionDTO;
import com.hyperbrains.hms.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link OrderExecution} and its DTO {@link OrderExecutionDTO}.
 */
@Mapper(componentModel = "spring")
public interface OrderExecutionMapper extends EntityMapper<OrderExecutionDTO, OrderExecution> {
    @Mapping(target = "doctorOrder", source = "doctorOrder", qualifiedByName = "doctorOrderId")
    @Mapping(target = "executedBy", source = "executedBy", qualifiedByName = "userLogin")
    OrderExecutionDTO toDto(OrderExecution s);

    @Named("doctorOrderId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    DoctorOrderDTO toDtoDoctorOrderId(DoctorOrder doctorOrder);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);
}
