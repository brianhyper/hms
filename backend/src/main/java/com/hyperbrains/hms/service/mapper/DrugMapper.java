package com.hyperbrains.hms.service.mapper;

import com.hyperbrains.hms.domain.Drug;
import com.hyperbrains.hms.service.dto.DrugDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Drug} and its DTO {@link DrugDTO}.
 */
@Mapper(componentModel = "spring")
public interface DrugMapper extends EntityMapper<DrugDTO, Drug> {}
