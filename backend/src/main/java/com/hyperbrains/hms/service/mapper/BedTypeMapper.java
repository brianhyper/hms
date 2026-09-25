package com.hyperbrains.hms.service.mapper;

import com.hyperbrains.hms.domain.BedType;
import com.hyperbrains.hms.service.dto.BedTypeDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link BedType} and its DTO {@link BedTypeDTO}.
 */
@Mapper(componentModel = "spring")
public interface BedTypeMapper extends EntityMapper<BedTypeDTO, BedType> {}
