package com.hyperbrains.hms.service.mapper;

import com.hyperbrains.hms.domain.VitalSigns;
import com.hyperbrains.hms.service.dto.VitalSignsDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link VitalSigns} and its DTO {@link VitalSignsDTO}.
 */
@Mapper(componentModel = "spring")
public interface VitalSignsMapper extends EntityMapper<VitalSignsDTO, VitalSigns> {}
