package com.example.services.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StopMapper {

    com.example.domain.entities.Stop toEntity(com.example.api.dto.StopDTOs.CreateStopRequest dto);

    @Mapping(target = "routeId", source = "route.id")
    com.example.api.dto.StopDTOs.StopResponse toResponse(com.example.domain.entities.Stop entity);
}
