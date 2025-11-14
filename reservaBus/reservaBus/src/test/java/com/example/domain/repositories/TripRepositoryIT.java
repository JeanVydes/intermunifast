package com.example.domain.repositories;

import com.example.domain.entities.Bus;
import com.example.domain.entities.Route;
import com.example.domain.entities.Trip;
import com.example.domain.enums.BusStatus;
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

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestJpaConfiguration.class)
@DisplayName("Trip Repository Integration Tests")
class TripRepositoryIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private BusRepository busRepository;

    private Route route;
    private Bus bus;

    @BeforeEach
    void setUp() {
        route = Route.builder()
                .name("Test Route")
                .code("TST001")
                .origin("City A")
                .destination("City B")
                .distanceKm(150.0)
                .durationMinutes(180)
                .pricePerKm(1.5)
                .build();
        route = routeRepository.save(route);

        bus = Bus.builder()
                .plate("BUS001")
                .capacity(45)
                .status(BusStatus.ACTIVE)
                .build();
        bus = busRepository.save(bus);
    }

    @Test
    @DisplayName("Should save and find trip")
    void shouldSaveAndFindTrip() {
        // Given
        Trip trip = Trip.builder()
                .date(LocalDate.now())
                .route(route)
                .bus(bus)
                .build();

        // When
        Trip saved = tripRepository.save(trip);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getDate()).isEqualTo(LocalDate.now());
        assertThat(saved.getRoute().getId()).isEqualTo(route.getId());
        assertThat(saved.getBus().getId()).isEqualTo(bus.getId());
    }

    @Test
    @DisplayName("Should find trips by route ID")
    void shouldFindTripsByRouteId() {
        // Given
        Trip trip1 = Trip.builder()
                .date(LocalDate.now())
                .route(route)
                .bus(bus)
                .build();

        Trip trip2 = Trip.builder()
                .date(LocalDate.now().plusDays(1))
                .route(route)
                .bus(bus)
                .build();

        tripRepository.save(trip1);
        tripRepository.save(trip2);

        // When
        List<Trip> found = tripRepository.findByRoute_IdAndDate(route.getId(), LocalDate.now());

        // Then
        assertThat(found).hasSize(1);
        assertThat(found).allMatch(t -> t.getRoute().getId().equals(route.getId()));
    }

    @Test
    @DisplayName("Should find trips by route ID and date")
    void shouldFindTripsByRouteIdAndDate() {
        // Given
        LocalDate today = LocalDate.now();
        Trip trip = Trip.builder()
                .date(today)
                .route(route)
                .bus(bus)
                .build();
        tripRepository.save(trip);

        // When
        List<Trip> found = tripRepository.findByRoute_IdAndDate(route.getId(), today);

        // Then
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getDate()).isEqualTo(today);
    }
}
