package com.example.domain.repositories;

import com.example.domain.entities.Bus;
import com.example.domain.entities.Route;
import com.example.domain.entities.Trip;
import com.example.domain.enums.BusStatus;
import com.example.domain.enums.TripStatus;
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

import java.time.LocalDateTime;
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
                LocalDateTime departure = LocalDateTime.now().plusHours(2);
                LocalDateTime arrival = departure.plusHours(3);

                Trip trip = Trip.builder()
                                .departureAt(departure)
                                .arrivalAt(arrival)
                                .route(route)
                                .bus(bus)
                                .status(TripStatus.SCHEDULED)
                                .build();

                // When
                Trip saved = tripRepository.save(trip);

                // Then
                assertThat(saved.getId()).isNotNull();
                assertThat(saved.getDepartureAt()).isEqualTo(departure);
                assertThat(saved.getArrivalAt()).isEqualTo(arrival);
                assertThat(saved.getStatus()).isEqualTo(TripStatus.SCHEDULED);
                assertThat(saved.getRoute().getId()).isEqualTo(route.getId());
                assertThat(saved.getBus().getId()).isEqualTo(bus.getId());
        }

        @Test
        @DisplayName("Should find trips by route ID")
        void shouldFindTripsByRouteId() {
                // Given
                LocalDateTime now = LocalDateTime.now();

                Trip trip1 = Trip.builder()
                                .departureAt(now.plusHours(2))
                                .arrivalAt(now.plusHours(5))
                                .route(route)
                                .bus(bus)
                                .status(TripStatus.SCHEDULED)
                                .build();

                Trip trip2 = Trip.builder()
                                .departureAt(now.plusDays(1).plusHours(2))
                                .arrivalAt(now.plusDays(1).plusHours(5))
                                .route(route)
                                .bus(bus)
                                .status(TripStatus.SCHEDULED)
                                .build();

                tripRepository.save(trip1);
                tripRepository.save(trip2);

                // When
                List<Trip> found = tripRepository.findByRoute_Id(route.getId());

                // Then
                assertThat(found)
                                .hasSize(2)
                                .allMatch(t -> t.getRoute().getId().equals(route.getId()));
        }

        @Test
        @DisplayName("Should find trips by route ID and departure date")
        void shouldFindTripsByRouteIdAndDepartureDate() {
                // Given
                LocalDateTime today = LocalDateTime.now().withHour(10).withMinute(0).withSecond(0).withNano(0);
                LocalDateTime tomorrow = today.plusDays(1);

                Trip trip1 = Trip.builder()
                                .departureAt(today)
                                .arrivalAt(today.plusHours(3))
                                .route(route)
                                .bus(bus)
                                .status(TripStatus.SCHEDULED)
                                .build();

                Trip trip2 = Trip.builder()
                                .departureAt(tomorrow)
                                .arrivalAt(tomorrow.plusHours(3))
                                .route(route)
                                .bus(bus)
                                .status(TripStatus.SCHEDULED)
                                .build();

                tripRepository.save(trip1);
                tripRepository.save(trip2);

                // When
                List<Trip> found = tripRepository.findByRouteIdAndDepartureDate(route.getId(), today);

                // Then
                assertThat(found)
                                .hasSize(1)
                                .first()
                                .satisfies(trip -> {
                                        assertThat(trip.getDepartureAt().toLocalDate()).isEqualTo(today.toLocalDate());
                                        assertThat(trip.getRoute().getId()).isEqualTo(route.getId());
                                });
        }
}
