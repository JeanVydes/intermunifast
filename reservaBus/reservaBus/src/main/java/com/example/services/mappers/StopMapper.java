package com.example.services.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.example.api.dto.StopDTOs;
import com.example.domain.entities.Stop;



@Mapper(componentModel = "spring")
public interface StopMapper {

    @Mapping(target = "id", ignore = true)
    Stop toEntity(StopDTOs.CreateStopRequest dto);
    
    StopDTOs.StopResponse toResponse(Stop entity);

    void patch(@MappingTarget Stop entity, StopDTOs.UpdateStopRequest dto);
}
