package com.hyperbrains.hms.service.mapper;

import com.hyperbrains.hms.domain.Bill;
import com.hyperbrains.hms.domain.PaymentPlan;
import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.service.dto.BillDTO;
import com.hyperbrains.hms.service.dto.PaymentPlanDTO;
import com.hyperbrains.hms.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link PaymentPlan} and its DTO {@link PaymentPlanDTO}.
 */
@Mapper(componentModel = "spring")
public interface PaymentPlanMapper extends EntityMapper<PaymentPlanDTO, PaymentPlan> {
    @Mapping(target = "bill", source = "bill", qualifiedByName = "billId")
    @Mapping(target = "agreedBy", source = "agreedBy", qualifiedByName = "userLogin")
    PaymentPlanDTO toDto(PaymentPlan s);

    @Named("billId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    BillDTO toDtoBillId(Bill bill);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);
}
