package com.example.services.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.example.api.dto.TicketDTOs;
import com.example.domain.entities.Ticket;


@Mapper(componentModel = "spring")
public interface TicketMapper {

    @Mapping(target = "id", ignore = true)
    Ticket toEntity(TicketDTOs.CreateTicketRequest dto);
    
    TicketDTOs.TicketResponse toResponse(Ticket entity);

    void patch(@MappingTarget Ticket entity, TicketDTOs.UpdateTicketRequest dto);
}
