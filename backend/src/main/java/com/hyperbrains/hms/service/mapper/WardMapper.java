package com.hyperbrains.hms.service.mapper;

import com.hyperbrains.hms.domain.Department;
import com.hyperbrains.hms.domain.Ward;
import com.hyperbrains.hms.service.dto.DepartmentDTO;
import com.hyperbrains.hms.service.dto.WardDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Ward} and its DTO {@link WardDTO}.
 */
@Mapper(componentModel = "spring")
public interface WardMapper extends EntityMapper<WardDTO, Ward> {
    @Mapping(target = "department", source = "department", qualifiedByName = "departmentId")
    WardDTO toDto(Ward s);

    @Named("departmentId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    DepartmentDTO toDtoDepartmentId(Department department);
}
