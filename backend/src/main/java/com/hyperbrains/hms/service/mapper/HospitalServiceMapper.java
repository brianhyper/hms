package com.hyperbrains.hms.service.mapper;

import com.hyperbrains.hms.domain.HospitalService;
import com.hyperbrains.hms.service.dto.HospitalServiceDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link HospitalService} and its DTO {@link HospitalServiceDTO}.
 */
@Mapper(componentModel = "spring")
public interface HospitalServiceMapper extends EntityMapper<HospitalServiceDTO, HospitalService> {}
