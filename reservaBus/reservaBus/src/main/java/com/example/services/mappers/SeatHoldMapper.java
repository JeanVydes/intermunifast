package com.example.services.mappers;

import java.util.Optional;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.example.api.dto.SeatHoldDTOs;
import com.example.domain.entities.SeatHold;
import com.example.domain.entities.Stop;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SeatHoldMapper {

    @Mapping(target = "fromStop", ignore = true)
    @Mapping(target = "toStop", ignore = true)
    SeatHold toEntity(SeatHoldDTOs.CreateSeatHoldRequest dto);

    @Mapping(source = "trip.id", target = "tripId")
    @Mapping(source = "fromStop", target = "fromStopId")
    @Mapping(source = "toStop", target = "toStopId")
    SeatHoldDTOs.SeatHoldResponse toResponse(SeatHold entity);

    default Optional<Long> map(Stop stop) {
        return stop != null ? Optional.of(stop.getId()) : Optional.empty();
    }
}
