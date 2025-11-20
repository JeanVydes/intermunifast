package com.example.services.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.example.api.dto.TicketDTOs;
import com.example.domain.entities.Ticket;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TicketMapper {

    Ticket toEntity(TicketDTOs.CreateTicketRequest dto);

    @Mapping(target = "status", expression = "java(entity.getStatus().toString())")
    @Mapping(target = "tripId", source = "trip.id")
    @Mapping(target = "fromStopId", expression = "java(entity.getFromStop() != null ? java.util.Optional.of(entity.getFromStop().getId()) : java.util.Optional.empty())")
    @Mapping(target = "toStopId", expression = "java(entity.getToStop() != null ? java.util.Optional.of(entity.getToStop().getId()) : java.util.Optional.empty())")
    TicketDTOs.TicketResponse toResponse(Ticket entity);

    void patch(@MappingTarget Ticket entity, TicketDTOs.UpdateTicketRequest dto);
}
