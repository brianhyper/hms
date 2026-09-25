package com.hyperbrains.hms.service.mapper;

import com.hyperbrains.hms.domain.Bill;
import com.hyperbrains.hms.domain.Payment;
import com.hyperbrains.hms.service.dto.BillDTO;
import com.hyperbrains.hms.service.dto.PaymentDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Bill} and its DTO {@link BillDTO}.
 */
@Mapper(componentModel = "spring")
public interface BillMapper extends EntityMapper<BillDTO, Bill> {
    @Mapping(target = "payment", source = "payment", qualifiedByName = "paymentId")
    BillDTO toDto(Bill s);

    @Named("paymentId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    PaymentDTO toDtoPaymentId(Payment payment);
}
