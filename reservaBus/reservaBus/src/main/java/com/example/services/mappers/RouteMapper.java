package com.example.services.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.api.dto.RouteDTOs;
import com.example.domain.entities.Route;

@Mapper(componentModel = "spring")
public interface RouteMapper {

    @Mapping(target = "id", ignore = true)
    Route toEntity(RouteDTOs.CreateRouteRequest dto);

    RouteDTOs.RouteResponse toResponse(Route entity);
}
