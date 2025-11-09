package com.example.api.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.api.dto.ConfigDTOs;

@RestController
@RequestMapping("/api/configs")
public class ConfigController {
    private final ConfigService configService;

    public ConfigController(ConfigService configService) {
        this.configService = configService;
    }

    @PostMapping
    public ResponseEntity<ConfigDTOs.ConfigResponse> create(
            @Validated @RequestBody ConfigDTOs.CreateConfigRequest req,
            UriComponentsBuilder uriBuilder) {
        ConfigDTOs.ConfigResponse createdConfig = configService.createConfig(req);
        return ResponseEntity.created(
                uriBuilder.path("/api/configs/{id}").buildAndExpand(createdConfig.id()).toUri())
                .body(createdConfig);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConfigDTOs.ConfigResponse> getById(@PathVariable Long id) {
        ConfigDTOs.ConfigResponse config = configService.getConfigById(id);
        return ResponseEntity.ok(config);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ConfigDTOs.ConfigResponse> update(
            @PathVariable Long id,
            @Validated @RequestBody ConfigDTOs.UpdateConfigRequest req) {
        ConfigDTOs.ConfigResponse updatedConfig = configService.updateConfig(id, req);
        return ResponseEntity.ok(updatedConfig);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        configService.deleteConfig(id);
        return ResponseEntity.noContent().build();
    }
}
