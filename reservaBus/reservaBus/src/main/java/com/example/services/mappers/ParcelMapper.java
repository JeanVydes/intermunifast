package com.example.services.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.example.api.dto.ParcelDTOs;
import com.example.domain.entities.Parcel;







@Mapper(componentModel = "spring")
public interface ParcelMapper {

    @Mapping(target = "id", ignore = true)
    Parcel toEntity(ParcelDTOs.CreateParcelRequest dto);
    
    ParcelDTOs.ParcelResponse toResponse(Parcel entity);

    void patch(@MappingTarget Parcel entity, ParcelDTOs.UpdateParcelRequest dto);
}
