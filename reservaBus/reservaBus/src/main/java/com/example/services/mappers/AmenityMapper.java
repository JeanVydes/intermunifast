package com.example.services.mappers;

import org.mapstruct.Mapper;

import com.example.api.dto.AmenityDTOs;
import com.example.domain.entities.Amenity;






@Mapper(componentModel = "spring")
public interface AmenityMapper {
    
    AmenityDTOs.AmenityResponse toResponse(Amenity entity);
}
