package com.example.services.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.api.dto.SeatHoldDTOs;
import com.example.domain.entities.SeatHold;

@Mapper(componentModel = "spring")
public interface SeatHoldMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "trip", ignore = true)
    @Mapping(target = "account", ignore = true)
    @Mapping(target = "fromStop", ignore = true)
    @Mapping(target = "toStop", ignore = true)
    SeatHold toEntity(SeatHoldDTOs.CreateSeatHoldRequest dto);

    @Mapping(source = "trip.id", target = "tripId")
    @Mapping(source = "fromStop.id", target = "fromStopId")
    @Mapping(source = "toStop.id", target = "toStopId")
    SeatHoldDTOs.SeatHoldResponse toResponse(SeatHold entity);
}
