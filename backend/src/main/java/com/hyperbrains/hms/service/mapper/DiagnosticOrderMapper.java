package com.hyperbrains.hms.service.mapper;

import com.hyperbrains.hms.domain.DiagnosticOrder;
import com.hyperbrains.hms.domain.LabTest;
import com.hyperbrains.hms.domain.RadiologyExam;
import com.hyperbrains.hms.domain.Result;
import com.hyperbrains.hms.domain.User;
import com.hyperbrains.hms.domain.Visit;
import com.hyperbrains.hms.service.dto.DiagnosticOrderDTO;
import com.hyperbrains.hms.service.dto.LabTestDTO;
import com.hyperbrains.hms.service.dto.RadiologyExamDTO;
import com.hyperbrains.hms.service.dto.ResultDTO;
import com.hyperbrains.hms.service.dto.UserDTO;
import com.hyperbrains.hms.service.dto.VisitDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link DiagnosticOrder} and its DTO {@link DiagnosticOrderDTO}.
 */
@Mapper(componentModel = "spring")
public interface DiagnosticOrderMapper extends EntityMapper<DiagnosticOrderDTO, DiagnosticOrder> {
    @Mapping(target = "result", source = "result", qualifiedByName = "resultId")
    @Mapping(target = "visit", source = "visit", qualifiedByName = "visitId")
    @Mapping(target = "orderedBy", source = "orderedBy", qualifiedByName = "userLogin")
    @Mapping(target = "labTest", source = "labTest", qualifiedByName = "labTestId")
    @Mapping(target = "radiologyExam", source = "radiologyExam", qualifiedByName = "radiologyExamId")
    DiagnosticOrderDTO toDto(DiagnosticOrder s);

    @Named("resultId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    ResultDTO toDtoResultId(Result result);

    @Named("visitId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    VisitDTO toDtoVisitId(Visit visit);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);

    @Named("labTestId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    LabTestDTO toDtoLabTestId(LabTest labTest);

    @Named("radiologyExamId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    RadiologyExamDTO toDtoRadiologyExamId(RadiologyExam radiologyExam);
}
