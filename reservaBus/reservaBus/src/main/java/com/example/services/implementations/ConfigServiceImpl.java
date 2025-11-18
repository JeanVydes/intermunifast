package com.example.services.implementations;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.api.dto.ConfigDTOs;
import com.example.domain.repositories.ConfigRepository;
import com.example.exceptions.NotFoundException;
import com.example.services.definitions.ConfigService;
import com.example.services.mappers.ConfigMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional

public class ConfigServiceImpl implements ConfigService {

    private final ConfigRepository repo;
    private final ConfigMapper mapper;

    @Override 
    public ConfigDTOs.ConfigResponse createConfig(ConfigDTOs.CreateConfigRequest req) {
        var config = mapper.toEntity(req);
        return mapper.toResponse(repo.save(config));
    }

    @Override
    @Transactional(readOnly = true)
    public ConfigDTOs.ConfigResponse getConfigById(Long id) {
        return repo.findById(id).map(mapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Config %d not found".formatted(id)));
    }

    @Override
    public void deleteConfig(Long id) {
        var config = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Config %d not found".formatted(id)));
        repo.delete(config);
    }

    @Override
    public ConfigDTOs.ConfigResponse updateConfig(Long id, ConfigDTOs.UpdateConfigRequest req) {
        var config = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Config %d not found".formatted(id)));
        mapper.patch( config, req);
        return mapper.toResponse(repo.save(config));
    }
}
