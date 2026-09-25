package com.hyperbrains.hms.service.mapper;

import com.hyperbrains.hms.domain.RadiologyExam;
import com.hyperbrains.hms.service.dto.RadiologyExamDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link RadiologyExam} and its DTO {@link RadiologyExamDTO}.
 */
@Mapper(componentModel = "spring")
public interface RadiologyExamMapper extends EntityMapper<RadiologyExamDTO, RadiologyExam> {}
