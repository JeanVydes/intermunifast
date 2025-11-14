package com.example.domain.repositories;

import com.example.domain.entities.Route;
import com.example.domain.entities.Stop;
import org.junit.jupiter.api.BeforeEach;
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
@DisplayName("Stop Repository Integration Tests")
class StopRepositoryIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private StopRepository stopRepository;

    @Autowired
    private RouteRepository routeRepository;

    private Route route;

    @BeforeEach
    void setUp() {
        route = Route.builder()
                .code("RT001")
                .name("Route Test")
                .origin("City A")
                .destination("City B")
                .durationMinutes(120)
                .distanceKm(100.0)
                .pricePerKm(0.5)
                .build();
        route = routeRepository.save(route);
    }

    @Test
    @DisplayName("Should save and retrieve stop")
    void shouldSaveAndRetrieveStop() {
        // Given
        Stop stop = Stop.builder()
                .name("Stop 1")
                .sequence(1)
                .latitude(12.34)
                .longitude(56.78)
                .route(route)
                .build();

        // When
        Stop saved = stopRepository.save(stop);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Stop 1");
        assertThat(saved.getSequence()).isEqualTo(1);
        assertThat(saved.getLatitude()).isEqualTo(12.34);
        assertThat(saved.getLongitude()).isEqualTo(56.78);
        assertThat(saved.getRoute().getId()).isEqualTo(route.getId());
    }

    @Test
    @DisplayName("Should find stops by route ID ordered by sequence")
    void shouldFindStopsByRouteIdOrderedBySequence() {
        // Given
        Stop stop1 = Stop.builder()
                .name("Stop C")
                .sequence(3)
                .latitude(12.0)
                .longitude(56.0)
                .route(route)
                .build();

        Stop stop2 = Stop.builder()
                .name("Stop A")
                .sequence(1)
                .latitude(13.0)
                .longitude(57.0)
                .route(route)
                .build();

        Stop stop3 = Stop.builder()
                .name("Stop B")
                .sequence(2)
                .latitude(14.0)
                .longitude(58.0)
                .route(route)
                .build();

        stopRepository.save(stop1);
        stopRepository.save(stop2);
        stopRepository.save(stop3);

        // When
        List<Stop> stops = stopRepository.findByRoute_IdOrderBySequenceAsc(route.getId());

        // Then
        assertThat(stops).hasSize(3);
        assertThat(stops.get(0).getName()).isEqualTo("Stop A");
        assertThat(stops.get(0).getSequence()).isEqualTo(1);
        assertThat(stops.get(1).getName()).isEqualTo("Stop B");
        assertThat(stops.get(1).getSequence()).isEqualTo(2);
        assertThat(stops.get(2).getName()).isEqualTo("Stop C");
        assertThat(stops.get(2).getSequence()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should return empty list when no stops for route")
    void shouldReturnEmptyListWhenNoStopsForRoute() {
        // When
        List<Stop> stops = stopRepository.findByRoute_IdOrderBySequenceAsc(999L);

        // Then
        assertThat(stops).isEmpty();
    }
}
