package com.hyperbrains.hms.service.mapper;

import com.hyperbrains.hms.domain.Bed;
import com.hyperbrains.hms.domain.BedType;
import com.hyperbrains.hms.domain.Ward;
import com.hyperbrains.hms.service.dto.BedDTO;
import com.hyperbrains.hms.service.dto.BedTypeDTO;
import com.hyperbrains.hms.service.dto.WardDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Bed} and its DTO {@link BedDTO}.
 */
@Mapper(componentModel = "spring")
public interface BedMapper extends EntityMapper<BedDTO, Bed> {
    @Mapping(target = "ward", source = "ward", qualifiedByName = "wardId")
    @Mapping(target = "bedType", source = "bedType", qualifiedByName = "bedTypeId")
    BedDTO toDto(Bed s);

    @Named("wardId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    WardDTO toDtoWardId(Ward ward);

    @Named("bedTypeId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    BedTypeDTO toDtoBedTypeId(BedType bedType);
}
