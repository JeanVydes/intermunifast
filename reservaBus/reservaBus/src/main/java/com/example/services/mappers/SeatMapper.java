package com.example.services.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.example.api.dto.SeatDTOs;
import com.example.domain.entities.Seat;



@Mapper(componentModel = "spring")
public interface SeatMapper {

    @Mapping(target = "id", ignore = true)
    Seat toEntity(SeatDTOs.CreateSeatRequest dto);
    
    SeatDTOs.SeatResponse toResponse(Seat    entity);

    void patch(@MappingTarget Seat entity, SeatDTOs.UpdateSeatRequest dto);
}
