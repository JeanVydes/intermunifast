package com.example.services.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.example.api.dto.TripDTOs;
import com.example.domain.entities.Trip;


@Mapper(componentModel = "spring")
public interface TripMapper {

    @Mapping(target = "id", ignore = true)
    Trip toEntity(TripDTOs.CreateTripRequest dto);
    
    TripDTOs.TripResponse toResponse(Trip entity);

    void patch(@MappingTarget Trip entity, TripDTOs.UpdateTripRequest dto);
}
