package com.example.services.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.example.api.dto.RouteDTOs;
import com.example.domain.entities.Route;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RouteMapper {

    Route toEntity(RouteDTOs.CreateRouteRequest dto);

    RouteDTOs.RouteResponse toResponse(Route entity);
}
