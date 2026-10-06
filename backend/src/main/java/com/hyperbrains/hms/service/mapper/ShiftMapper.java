package com.hyperbrains.hms.service.mapper;

import com.hyperbrains.hms.domain.Shift;
import com.hyperbrains.hms.domain.StaffRecord;
import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.domain.Ward;
import com.hyperbrains.hms.service.dto.ShiftDTO;
import com.hyperbrains.hms.service.dto.StaffRecordDTO;
import com.hyperbrains.hms.service.dto.UserDTO;
import com.hyperbrains.hms.service.dto.WardDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Shift} and its DTO {@link ShiftDTO}.
 *
 * <p>Every relation goes out as an id, which is what keeps a staff file out of a roster row: the shift names the
 * person by id, and reading their employment record is a separate request that has its own access rule.
 */
@Mapper(componentModel = "spring")
public interface ShiftMapper extends EntityMapper<ShiftDTO, Shift> {
    @Mapping(target = "staffRecord", source = "staffRecord", qualifiedByName = "staffRecordId")
    @Mapping(target = "ward", source = "ward", qualifiedByName = "wardId")
    @Mapping(target = "createdBy", source = "createdBy", qualifiedByName = "userLogin")
    ShiftDTO toDto(Shift s);

    @Named("staffRecordId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    StaffRecordDTO toDtoStaffRecordId(StaffRecord staffRecord);

    @Named("wardId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    WardDTO toDtoWardId(Ward ward);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);
}
