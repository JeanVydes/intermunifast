package com.example.services.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.example.api.dto.ConfigDTOs;
import com.example.domain.entities.Config;



@Mapper(componentModel = "spring")
public interface ConfigMapper {

    @Mapping(target = "id", ignore = true)
    Config toEntity(ConfigDTOs.CreateConfigRequest dto);
    
    ConfigDTOs.ConfigResponse toResponse(Config entity);

    void patch(@MappingTarget Config entity, ConfigDTOs.UpdateConfigRequest dto);
}
