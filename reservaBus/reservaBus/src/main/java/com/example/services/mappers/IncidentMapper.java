package com.example.services.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.example.api.dto.IncidentDTOs;
import com.example.domain.entities.Incident;



@Mapper(componentModel = "spring")
public interface IncidentMapper {

    @Mapping(target = "id", ignore = true)
    Incident toEntity(IncidentDTOs.CreateIncidentRequest dto);
    
    IncidentDTOs.IncidentResponse toResponse(Incident entity);

    void patch(@MappingTarget Incident entity, IncidentDTOs.UpdateIncidentRequest dto);
}
