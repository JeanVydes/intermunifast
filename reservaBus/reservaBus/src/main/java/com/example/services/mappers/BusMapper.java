package com.example.services.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.example.api.dto.BusDTOs;
import com.example.domain.entities.Bus;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BusMapper {

    Bus toEntity(BusDTOs.CreateBusRequest dto);

    BusDTOs.BusResponse toResponse(Bus entity);
}
