package com.hyperbrains.hms.service.mapper;

import com.hyperbrains.hms.domain.LabTest;
import com.hyperbrains.hms.service.dto.LabTestDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link LabTest} and its DTO {@link LabTestDTO}.
 */
@Mapper(componentModel = "spring")
public interface LabTestMapper extends EntityMapper<LabTestDTO, LabTest> {}
