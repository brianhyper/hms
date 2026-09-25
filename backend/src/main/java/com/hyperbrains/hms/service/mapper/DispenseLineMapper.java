package com.hyperbrains.hms.service.mapper;

import com.hyperbrains.hms.domain.Dispense;
import com.hyperbrains.hms.domain.DispenseLine;
import com.hyperbrains.hms.domain.Drug;
import com.hyperbrains.hms.domain.PrescriptionLine;
import com.hyperbrains.hms.service.dto.DispenseDTO;
import com.hyperbrains.hms.service.dto.DispenseLineDTO;
import com.hyperbrains.hms.service.dto.DrugDTO;
import com.hyperbrains.hms.service.dto.PrescriptionLineDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link DispenseLine} and its DTO {@link DispenseLineDTO}.
 */
@Mapper(componentModel = "spring")
public interface DispenseLineMapper extends EntityMapper<DispenseLineDTO, DispenseLine> {
    @Mapping(target = "dispense", source = "dispense", qualifiedByName = "dispenseId")
    @Mapping(target = "prescriptionLine", source = "prescriptionLine", qualifiedByName = "prescriptionLineId")
    @Mapping(target = "drug", source = "drug", qualifiedByName = "drugId")
    DispenseLineDTO toDto(DispenseLine s);

    @Named("dispenseId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    DispenseDTO toDtoDispenseId(Dispense dispense);

    @Named("prescriptionLineId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    PrescriptionLineDTO toDtoPrescriptionLineId(PrescriptionLine prescriptionLine);

    @Named("drugId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    DrugDTO toDtoDrugId(Drug drug);
}
