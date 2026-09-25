package com.hyperbrains.hms.service.mapper;

import com.hyperbrains.hms.domain.Bill;
import com.hyperbrains.hms.domain.Consultation;
import com.hyperbrains.hms.domain.Patient;
import com.hyperbrains.hms.domain.Visit;
import com.hyperbrains.hms.domain.VitalSigns;
import com.hyperbrains.hms.service.dto.BillDTO;
import com.hyperbrains.hms.service.dto.ConsultationDTO;
import com.hyperbrains.hms.service.dto.PatientDTO;
import com.hyperbrains.hms.service.dto.VisitDTO;
import com.hyperbrains.hms.service.dto.VitalSignsDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Visit} and its DTO {@link VisitDTO}.
 */
@Mapper(componentModel = "spring")
public interface VisitMapper extends EntityMapper<VisitDTO, Visit> {
    @Mapping(target = "vitals", source = "vitals", qualifiedByName = "vitalSignsId")
    @Mapping(target = "consultation", source = "consultation", qualifiedByName = "consultationId")
    @Mapping(target = "bill", source = "bill", qualifiedByName = "billId")
    @Mapping(target = "patient", source = "patient", qualifiedByName = "patientId")
    VisitDTO toDto(Visit s);

    @Named("vitalSignsId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    VitalSignsDTO toDtoVitalSignsId(VitalSigns vitalSigns);

    @Named("consultationId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    ConsultationDTO toDtoConsultationId(Consultation consultation);

    @Named("billId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    BillDTO toDtoBillId(Bill bill);

    @Named("patientId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    PatientDTO toDtoPatientId(Patient patient);
}
