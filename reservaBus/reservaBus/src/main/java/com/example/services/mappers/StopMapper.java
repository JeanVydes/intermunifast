package com.example.services.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.example.api.dto.StopDTOs;
import com.example.domain.entities.Stop;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StopMapper {

    @Mapping(target = "id", ignore = true)
    Stop toEntity(StopDTOs.CreateStopRequest dto);

    @Mapping(target = "routeId", source = "route.id")
    StopDTOs.StopResponse toResponse(Stop entity);
}
