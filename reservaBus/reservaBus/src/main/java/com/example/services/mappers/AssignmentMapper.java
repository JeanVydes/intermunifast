package com.example.services.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.example.api.dto.AssignmentDTOs;
import com.example.domain.entities.Assignment;



@Mapper(componentModel = "spring")
public interface AssignmentMapper {

    @Mapping(target = "id", ignore = true)
    Assignment toEntity(AssignmentDTOs.CreateAssignmentRequest dto);
    
    AssignmentDTOs.AssignmentResponse toResponse(Assignment entity);

    void patch(@MappingTarget Assignment entity, AssignmentDTOs.UpdateAssignmentRequest dto);
}
