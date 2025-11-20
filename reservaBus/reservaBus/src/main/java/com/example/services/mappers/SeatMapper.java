package com.example.services.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.example.api.dto.SeatDTOs;
import com.example.domain.entities.Seat;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SeatMapper {

    Seat toEntity(SeatDTOs.CreateSeatRequest dto);

    SeatDTOs.SeatResponse toResponse(Seat entity);
}
