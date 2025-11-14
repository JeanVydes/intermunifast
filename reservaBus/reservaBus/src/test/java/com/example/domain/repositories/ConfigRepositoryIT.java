package com.example.domain.repositories;

import com.example.domain.entities.Config;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestJpaConfiguration.class)
@DisplayName("Config Repository Integration Tests")
class ConfigRepositoryIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private ConfigRepository configRepository;

    @Test
    @DisplayName("Should save and retrieve config")
    void shouldSaveAndRetrieveConfig() {
        // Given
        Config config = Config.builder()
                .key("app.name")
                .value("ReservaBus")
                .build();

        // When
        Config saved = configRepository.save(config);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getKey()).isEqualTo("app.name");
        assertThat(saved.getValue()).isEqualTo("ReservaBus");
    }

    @Test
    @DisplayName("Should find config by key")
    void shouldFindConfigByKey() {
        // Given
        Config config = Config.builder()
                .key("max.seats")
                .value("50")
                .build();
        configRepository.save(config);

        // When
        Optional<Config> found = configRepository.findByKey("max.seats");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getValue()).isEqualTo("50");
    }

    @Test
    @DisplayName("Should find all configs")
    void shouldFindAllConfigs() {
        // Given
        Config config1 = Config.builder()
                .key("setting1")
                .value("value1")
                .build();

        Config config2 = Config.builder()
                .key("setting2")
                .value("value2")
                .build();

        configRepository.save(config1);
        configRepository.save(config2);

        // When
        List<Config> configs = configRepository.findAll();

        // Then
        assertThat(configs).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("Should return empty when config key not found")
    void shouldReturnEmptyWhenConfigKeyNotFound() {
        // When
        Optional<Config> found = configRepository.findByKey("nonexistent.key");

        // Then
        assertThat(found).isEmpty();
    }
}
