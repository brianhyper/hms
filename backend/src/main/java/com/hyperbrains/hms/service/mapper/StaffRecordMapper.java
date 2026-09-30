package com.hyperbrains.hms.service.mapper;

import com.hyperbrains.hms.domain.Department;
import com.hyperbrains.hms.domain.StaffRecord;
import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.service.dto.DepartmentDTO;
import com.hyperbrains.hms.service.dto.StaffRecordDTO;
import com.hyperbrains.hms.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link StaffRecord} and its DTO {@link StaffRecordDTO}.
 *
 * <p>Both relations go out as an id and nothing else, the same shape the rest of the model uses: a staff record
 * carries no copy of a department's or an account's contents, so a rename in either place cannot leave a stale name
 * sitting inside an employment record.
 */
@Mapper(componentModel = "spring")
public interface StaffRecordMapper extends EntityMapper<StaffRecordDTO, StaffRecord> {
    @Mapping(target = "department", source = "department", qualifiedByName = "departmentId")
    @Mapping(target = "user", source = "user", qualifiedByName = "userId")
    StaffRecordDTO toDto(StaffRecord s);

    @Named("departmentId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    DepartmentDTO toDtoDepartmentId(Department department);

    @Named("userId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    UserDTO toDtoUserId(User user);
}
