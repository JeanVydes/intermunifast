package com.example.services.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import com.example.api.dto.BaggageDTOs;
import com.example.domain.entities.Baggage;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BaggageMapper {

    @Mapping(source = "weightKg", target = "weight")
    Baggage toEntity(BaggageDTOs.CreateBaggageRequest dto);

    @Mapping(source = "weight", target = "weightKg")
    BaggageDTOs.BaggageResponse toResponse(Baggage entity);

    void patch(@MappingTarget Baggage entity, BaggageDTOs.UpdateBaggageRequest dto);
}
