package com.hyperbrains.hms.service.mapper;

import com.hyperbrains.hms.domain.Bill;
import com.hyperbrains.hms.domain.BillLineItem;
import com.hyperbrains.hms.service.dto.BillDTO;
import com.hyperbrains.hms.service.dto.BillLineItemDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link BillLineItem} and its DTO {@link BillLineItemDTO}.
 */
@Mapper(componentModel = "spring")
public interface BillLineItemMapper extends EntityMapper<BillLineItemDTO, BillLineItem> {
    @Mapping(target = "bill", source = "bill", qualifiedByName = "billId")
    BillLineItemDTO toDto(BillLineItem s);

    @Named("billId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    BillDTO toDtoBillId(Bill bill);
}
