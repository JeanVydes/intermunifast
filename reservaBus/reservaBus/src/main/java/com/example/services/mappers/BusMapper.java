package com.example.services.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.api.dto.BusDTOs;
import com.example.domain.entities.Bus;

@Mapper(componentModel = "spring")
public interface BusMapper {

    @Mapping(target = "id", ignore = true)
    Bus toEntity(BusDTOs.CreateBusRequest dto);

    BusDTOs.BusResponse toResponse(Bus entity);
}
