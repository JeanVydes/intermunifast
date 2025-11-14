package com.example.services.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.example.api.dto.TripDTOs;
import com.example.domain.entities.Trip;

@Mapper(componentModel = "spring")
public interface TripMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "bus", ignore = true)
    @Mapping(target = "route", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "seatHolds", ignore = true)
    @Mapping(target = "tickets", ignore = true)
    @Mapping(target = "assignments", ignore = true)
    @Mapping(target = "incidents", ignore = true)
    Trip toEntity(TripDTOs.CreateTripRequest dto);

    @Mapping(target = "routeId", source = "route.id")
    @Mapping(target = "busId", source = "bus.id")
    TripDTOs.TripResponse toResponse(Trip entity);

    @Mapping(target = "route", ignore = true)
    @Mapping(target = "bus", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "seatHolds", ignore = true)
    @Mapping(target = "tickets", ignore = true)
    @Mapping(target = "assignments", ignore = true)
    @Mapping(target = "incidents", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void patch(@MappingTarget Trip entity, TripDTOs.UpdateTripRequest dto);
}
