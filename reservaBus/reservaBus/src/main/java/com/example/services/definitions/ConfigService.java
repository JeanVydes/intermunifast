package com.example.services.definitions;

import com.example.api.dto.ConfigDTOs;

public interface ConfigService {
    ConfigDTOs.ConfigResponse createConfig(ConfigDTOs.CreateConfigRequest req);

    ConfigDTOs.ConfigResponse getConfigById(Long id);

    ConfigDTOs.ConfigResponse updateConfig(Long id, ConfigDTOs.UpdateConfigRequest req);

    void deleteConfig(Long id);
}
