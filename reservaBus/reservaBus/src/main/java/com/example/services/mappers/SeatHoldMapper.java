package com.example.services.mappers;

import java.util.Optional;

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
    @Mapping(source = "fromStop", target = "fromStopId")
    @Mapping(source = "toStop", target = "toStopId")
    SeatHoldDTOs.SeatHoldResponse toResponse(SeatHold entity);

    default Optional<Long> mapStopToId(com.example.domain.entities.Stop stop) {
        return stop != null ? Optional.of(stop.getId()) : Optional.empty();
    }
}
