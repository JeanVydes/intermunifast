package com.example.domain.repositories;

import com.example.domain.entities.Route;
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

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestJpaConfiguration.class)
@DisplayName("Route Repository Integration Tests")
class RouteRepositoryIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private RouteRepository routeRepository;

    @Test
    @DisplayName("Should save and find route")
    void shouldSaveAndFindRoute() {
        // Given
        Route route = Route.builder()
                .name("Lima - Arequipa")
                .code("LIM-AQP")
                .origin("Lima")
                .destination("Arequipa")
                .distanceKm(1020.5)
                .durationMinutes(960)
                .pricePerKm(2.5)
                .build();

        // When
        Route saved = routeRepository.save(route);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Lima - Arequipa");
        assertThat(saved.getCode()).isEqualTo("LIM-AQP");
    }

    @Test
    @DisplayName("Should find routes by origin")
    void shouldFindRoutesByOrigin() {
        // Given
        Route route1 = Route.builder()
                .name("Lima - Cusco")
                .code("LIM-CUZ")
                .origin("Lima")
                .destination("Cusco")
                .distanceKm(1105.0)
                .durationMinutes(1200)
                .pricePerKm(2.0)
                .build();

        Route route2 = Route.builder()
                .name("Lima - Trujillo")
                .code("LIM-TRU")
                .origin("Lima")
                .destination("Trujillo")
                .distanceKm(560.0)
                .durationMinutes(540)
                .pricePerKm(1.8)
                .build();

        routeRepository.save(route1);
        routeRepository.save(route2);

        // When
        List<Route> found = routeRepository.findByOrigin("Lima");

        // Then
        assertThat(found).hasSize(2);
        assertThat(found).allMatch(r -> r.getOrigin().equals("Lima"));
    }

    @Test
    @DisplayName("Should find routes by destination")
    void shouldFindRoutesByDestination() {
        // Given
        Route route = Route.builder()
                .name("Arequipa - Puno")
                .code("AQP-PUN")
                .origin("Arequipa")
                .destination("Puno")
                .distanceKm(300.0)
                .durationMinutes(360)
                .pricePerKm(2.2)
                .build();
        routeRepository.save(route);

        // When
        List<Route> found = routeRepository.findByDestination("Puno");

        // Then
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getDestination()).isEqualTo("Puno");
    }

    @Test
    @DisplayName("Should find routes by origin and destination")
    void shouldFindRoutesByOriginAndDestination() {
        // Given
        Route route = Route.builder()
                .name("Cusco - Puno")
                .code("CUZ-PUN")
                .origin("Cusco")
                .destination("Puno")
                .distanceKm(389.0)
                .durationMinutes(420)
                .pricePerKm(1.9)
                .build();
        routeRepository.save(route);

        // When
        List<Route> found = routeRepository.findByOriginAndDestination("Cusco", "Puno");

        // Then
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getOrigin()).isEqualTo("Cusco");
        assertThat(found.get(0).getDestination()).isEqualTo("Puno");
    }
}
