package com.hyperbrains.hms.service.mapper;

import com.hyperbrains.hms.domain.Appointment;
import com.hyperbrains.hms.domain.Department;
import com.hyperbrains.hms.domain.Patient;
import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.domain.Visit;
import com.hyperbrains.hms.service.dto.AppointmentDTO;
import com.hyperbrains.hms.service.dto.DepartmentDTO;
import com.hyperbrains.hms.service.dto.PatientDTO;
import com.hyperbrains.hms.service.dto.UserDTO;
import com.hyperbrains.hms.service.dto.VisitDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Appointment} and its DTO {@link AppointmentDTO}.
 */
@Mapper(componentModel = "spring")
public interface AppointmentMapper extends EntityMapper<AppointmentDTO, Appointment> {
    @Mapping(target = "visit", source = "visit", qualifiedByName = "visitId")
    @Mapping(target = "patient", source = "patient", qualifiedByName = "patientId")
    @Mapping(target = "department", source = "department", qualifiedByName = "departmentId")
    @Mapping(target = "doctor", source = "doctor", qualifiedByName = "userLogin")
    AppointmentDTO toDto(Appointment s);

    @Named("visitId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    VisitDTO toDtoVisitId(Visit visit);

    @Named("patientId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    PatientDTO toDtoPatientId(Patient patient);

    @Named("departmentId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    DepartmentDTO toDtoDepartmentId(Department department);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);
}
