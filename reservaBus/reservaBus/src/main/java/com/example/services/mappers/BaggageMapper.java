package com.example.services.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.example.api.dto.BaggageDTOs;
import com.example.domain.entities.Baggage;

@Mapper(componentModel = "spring")
public interface BaggageMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "weightKg", target = "weight")
    Baggage toEntity(BaggageDTOs.CreateBaggageRequest dto);
    
    @Mapping(source = "weight", target = "weightKg")
    BaggageDTOs.BaggageResponse toResponse(Baggage entity);

    void patch(@MappingTarget Baggage entity, BaggageDTOs.UpdateBaggageRequest dto);
}
