package com.example.services.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.example.api.dto.SeatHoldDTOs;
import com.example.domain.entities.SeatHold;



@Mapper(componentModel = "spring")
public interface SeatHoldMapper {

    @Mapping(target = "id", ignore = true)
    SeatHold toEntity(SeatHoldDTOs.CreateSeatHoldRequest dto);
    
    SeatHoldDTOs.SeatHoldResponse toResponse(SeatHold entity);

    void patch(@MappingTarget SeatHold entity, SeatHoldDTOs.UpdateSeatHoldRequest dto);
}
