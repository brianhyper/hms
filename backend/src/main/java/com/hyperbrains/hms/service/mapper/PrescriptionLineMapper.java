package com.hyperbrains.hms.service.mapper;

import com.hyperbrains.hms.domain.Drug;
import com.hyperbrains.hms.domain.Prescription;
import com.hyperbrains.hms.domain.PrescriptionLine;
import com.hyperbrains.hms.service.dto.DrugDTO;
import com.hyperbrains.hms.service.dto.PrescriptionDTO;
import com.hyperbrains.hms.service.dto.PrescriptionLineDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link PrescriptionLine} and its DTO {@link PrescriptionLineDTO}.
 */
@Mapper(componentModel = "spring")
public interface PrescriptionLineMapper extends EntityMapper<PrescriptionLineDTO, PrescriptionLine> {
    @Mapping(target = "prescription", source = "prescription", qualifiedByName = "prescriptionId")
    @Mapping(target = "drug", source = "drug", qualifiedByName = "drugId")
    PrescriptionLineDTO toDto(PrescriptionLine s);

    @Named("prescriptionId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    PrescriptionDTO toDtoPrescriptionId(Prescription prescription);

    @Named("drugId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    DrugDTO toDtoDrugId(Drug drug);
}
