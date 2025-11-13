package com.example.services.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.example.api.dto.FareRuleDTOs;
import com.example.domain.entities.FareRule;



@Mapper(componentModel = "spring")
public interface FareRuleMapper {

    @Mapping(target = "id", ignore = true)
    FareRule toEntity(FareRuleDTOs.CreateFareRuleRequest dto);
    
    FareRuleDTOs.FareRuleResponse toResponse(FareRule entity);

    void patch(@MappingTarget FareRule entity, FareRuleDTOs.UpdateFareRuleRequest dto);
}
