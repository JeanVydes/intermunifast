package com.example.services.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import com.example.api.dto.TripDTOs;
import com.example.domain.entities.Trip;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TripMapper {

    Trip toEntity(TripDTOs.CreateTripRequest dto);

    @Mapping(target = "routeId", source = "route.id")
    @Mapping(target = "busId", source = "bus.id")
    TripDTOs.TripResponse toResponse(Trip entity);

    void patch(@MappingTarget Trip entity, TripDTOs.UpdateTripRequest dto);
}
