package com.example.domain.repositories;

import com.example.domain.entities.Account;
import com.example.domain.entities.Assignment;
import com.example.domain.entities.Bus;
import com.example.domain.entities.Route;
import com.example.domain.entities.Trip;
import com.example.domain.enums.AccountRole;
import com.example.domain.enums.AccountStatus;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestJpaConfiguration.class)
@DisplayName("Assignment Repository Integration Tests")
class AssignmentRepositoryIT {

        @Container
        @ServiceConnection
        static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

        @Autowired
        private AssignmentRepository assignmentRepository;

        @Autowired
        private AccountRepository accountRepository;

        @Autowired
        private TripRepository tripRepository;

        @Autowired
        private BusRepository busRepository;

        @Autowired
        private RouteRepository routeRepository;

        private Account driver;
        private Account dispatcher;
        private Trip trip;

        @BeforeEach
        void setUp() {
                // Create driver
                driver = Account.builder()
                                .name("John Driver")
                                .email("driver@test.com")
                                .phone("1234567890")
                                .passwordHash("hash123")
                                .role(AccountRole.DRIVER)
                                .status(AccountStatus.ACTIVE)
                                .build();
                driver = accountRepository.save(driver);

                // Create dispatcher
                dispatcher = Account.builder()
                                .name("Jane Dispatcher")
                                .email("dispatcher@test.com")
                                .phone("0987654321")
                                .passwordHash("hash456")
                                .role(AccountRole.DISPATCHER)
                                .status(AccountStatus.ACTIVE)
                                .build();
                dispatcher = accountRepository.save(dispatcher);

                // Create route
                Route route = Route.builder()
                                .name("Test Route")
                                .code("TR001")
                                .origin("City A")
                                .destination("City B")
                                .distanceKm(100.0)
                                .durationMinutes(120)
                                .pricePerKm(1.5)
                                .build();
                route = routeRepository.save(route);

                // Create bus
                Bus bus = Bus.builder()
                                .plate("ABC123")
                                .capacity(40)
                                .status(BusStatus.ACTIVE)
                                .build();
                bus = busRepository.save(bus);

                // Create trip
                LocalDateTime departure = LocalDateTime.now().plusHours(2);
                trip = Trip.builder()
                                .departureAt(departure)
                                .arrivalAt(departure.plusHours(3))
                                .route(route)
                                .bus(bus)
                                .status(com.example.domain.enums.TripStatus.SCHEDULED)
                                .build();
                trip = tripRepository.save(trip);
        }

        @Test
        @DisplayName("Should save and find assignment")
        void shouldSaveAndFindAssignment() {
                // Given
                Assignment assignment = Assignment.builder()
                                .driver(driver)
                                .dispatcher(dispatcher)
                                .trip(trip)
                                .assignedAt(LocalDateTime.now())
                                .checklistOk(true)
                                .build();

                // When
                Assignment saved = assignmentRepository.save(assignment);

                // Then
                assertThat(saved.getId()).isNotNull();
                assertThat(saved.getDriver().getId()).isEqualTo(driver.getId());
                assertThat(saved.getDispatcher().getId()).isEqualTo(dispatcher.getId());
                assertThat(saved.getTrip().getId()).isEqualTo(trip.getId());
                assertThat(saved.getChecklistOk()).isTrue();
        }

        @Test
        @DisplayName("Should find assignments by trip ID")
        void shouldFindAssignmentsByTripId() {
                // Given
                Assignment assignment = Assignment.builder()
                                .driver(driver)
                                .dispatcher(dispatcher)
                                .trip(trip)
                                .assignedAt(LocalDateTime.now())
                                .checklistOk(true)
                                .build();
                assignmentRepository.save(assignment);

                // When
                List<Assignment> found = assignmentRepository.findByTrip_Id(trip.getId());

                // Then
                assertThat(found).hasSize(1);
                assertThat(found.get(0).getTrip().getId()).isEqualTo(trip.getId());
        }

        @Test
        @DisplayName("Should find assignments by driver ID")
        void shouldFindAssignmentsByDriverId() {
                // Given
                Assignment assignment = Assignment.builder()
                                .driver(driver)
                                .dispatcher(dispatcher)
                                .trip(trip)
                                .assignedAt(LocalDateTime.now())
                                .checklistOk(false)
                                .build();
                assignmentRepository.save(assignment);

                // When
                List<Assignment> found = assignmentRepository.findByDriver_Id(driver.getId());

                // Then
                assertThat(found).hasSize(1);
                assertThat(found.get(0).getDriver().getId()).isEqualTo(driver.getId());
                assertThat(found.get(0).getChecklistOk()).isFalse();
        }
}
