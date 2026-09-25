package com.hyperbrains.hms.service.mapper;

import com.hyperbrains.hms.domain.Department;
import com.hyperbrains.hms.service.dto.DepartmentDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Department} and its DTO {@link DepartmentDTO}.
 */
@Mapper(componentModel = "spring")
public interface DepartmentMapper extends EntityMapper<DepartmentDTO, Department> {}
