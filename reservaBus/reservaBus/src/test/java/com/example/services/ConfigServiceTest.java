package com.example.services;

import com.example.api.dto.ConfigDTOs;
import com.example.domain.entities.Config;
import com.example.domain.repositories.ConfigRepository;
import com.example.exceptions.NotFoundException;
import com.example.services.definitions.ConfigServiceImpl;
import com.example.services.mappers.ConfigMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Config Service Unit Tests")
class ConfigServiceTest {

    @Mock
    private ConfigRepository configRepository;

    @Mock
    private ConfigMapper configMapper;

    @InjectMocks
    private ConfigServiceImpl configService;

    private Config config;
    private ConfigDTOs.ConfigResponse configResponse;
    private ConfigDTOs.CreateConfigRequest createRequest;
    private ConfigDTOs.UpdateConfigRequest updateRequest;

    @BeforeEach
    void setUp() {
        config = Config.builder()
                .id(1L)
                .key("app.name")
                .value("ReservaBus")
                .build();

        configResponse = new ConfigDTOs.ConfigResponse(1L, "app.name", "ReservaBus");
        createRequest = new ConfigDTOs.CreateConfigRequest("app.name", "ReservaBus");
        updateRequest = new ConfigDTOs.UpdateConfigRequest("app.version", "1.0.0");
    }

    @Test
    @DisplayName("Should create config successfully")
    void shouldCreateConfig() {
        // Given
        when(configMapper.toEntity(createRequest)).thenReturn(config);
        when(configRepository.save(config)).thenReturn(config);
        when(configMapper.toResponse(config)).thenReturn(configResponse);

        // When
        ConfigDTOs.ConfigResponse result = configService.createConfig(createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        verify(configRepository).save(config);
    }

    @Test
    @DisplayName("Should get config by ID successfully")
    void shouldGetConfigById() {
        // Given
        when(configRepository.findById(1L)).thenReturn(Optional.of(config));
        when(configMapper.toResponse(config)).thenReturn(configResponse);

        // When
        ConfigDTOs.ConfigResponse result = configService.getConfigById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.key()).isEqualTo("app.name");
        verify(configRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw NotFoundException when config not found by ID")
    void shouldThrowNotFoundExceptionWhenConfigNotFoundById() {
        // Given
        when(configRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> configService.getConfigById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Config 999 not found");
    }

    @Test
    @DisplayName("Should update config successfully")
    void shouldUpdateConfig() {
        // Given
        when(configRepository.findById(1L)).thenReturn(Optional.of(config));
        when(configRepository.save(config)).thenReturn(config);
        when(configMapper.toResponse(config)).thenReturn(configResponse);

        // When
        ConfigDTOs.ConfigResponse result = configService.updateConfig(1L, updateRequest);

        // Then
        assertThat(result).isNotNull();
        verify(configMapper).patch(config, updateRequest);
        verify(configRepository).save(config);
    }

    @Test
    @DisplayName("Should delete config successfully")
    void shouldDeleteConfig() {
        // Given
        when(configRepository.findById(1L)).thenReturn(Optional.of(config));

        // When
        configService.deleteConfig(1L);

        // Then
        verify(configRepository).delete(config);
    }

    @Test
    @DisplayName("Should throw NotFoundException when deleting non-existent config")
    void shouldThrowNotFoundExceptionWhenDeletingNonExistentConfig() {
        // Given
        when(configRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> configService.deleteConfig(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Config 999 not found");
    }
}
