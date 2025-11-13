package com.example.domain.repositories;

import com.example.domain.entities.*;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestJpaConfiguration.class)
@DisplayName("SeatHold Repository Integration Tests")
class SeatHoldRepositoryIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private SeatHoldRepository seatHoldRepository;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private BusRepository busRepository;

    private Trip trip;
    private Account account;

    @BeforeEach
    void setUp() {
        account = Account.builder()
                .name("Test Account")
                .email("seat@test.com")
                .phone("1234567890")
                .passwordHash("hash")
                .role(AccountRole.PASSENGER)
                .status(AccountStatus.ACTIVE)
                .build();
        account = accountRepository.save(account);

        Route route = Route.builder()
                .code("RT001")
                .name("Test Route")
                .origin("Origin")
                .destination("Destination")
                .durationMinutes(60)
                .distanceKm(50.0)
                .pricePerKm(0.5)
                .build();
        route = routeRepository.save(route);

        Bus bus = Bus.builder()
                .plate("BUS123")
                .capacity(40)
                .status(BusStatus.ACTIVE)
                .build();
        bus = busRepository.save(bus);

        trip = Trip.builder()
                .date(LocalDate.now())
                .route(route)
                .bus(bus)
                .build();
        trip = tripRepository.save(trip);
    }

    @Test
    @DisplayName("Should save and retrieve seat hold")
    void shouldSaveAndRetrieveSeatHold() {
        // Given
        SeatHold hold = SeatHold.builder()
                .seatNumber("A1")
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .trip(trip)
                .account(account)
                .build();

        // When
        SeatHold saved = seatHoldRepository.save(hold);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getSeatNumber()).isEqualTo("A1");
        assertThat(saved.getTrip().getId()).isEqualTo(trip.getId());
        assertThat(saved.getAccount().getId()).isEqualTo(account.getId());
    }

    @Test
    @DisplayName("Should find seat holds by trip ID")
    void shouldFindSeatHoldsByTripId() {
        // Given
        SeatHold hold1 = SeatHold.builder()
                .seatNumber("A1")
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .trip(trip)
                .account(account)
                .build();

        SeatHold hold2 = SeatHold.builder()
                .seatNumber("A2")
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .trip(trip)
                .account(account)
                .build();

        seatHoldRepository.save(hold1);
        seatHoldRepository.save(hold2);

        // When
        List<SeatHold> holds = seatHoldRepository.findByTrip_Id(trip.getId());

        // Then
        assertThat(holds).hasSize(2);
        assertThat(holds).extracting(SeatHold::getSeatNumber)
                .containsExactlyInAnyOrder("A1", "A2");
    }

    @Test
    @DisplayName("Should find seat hold by trip ID and seat number")
    void shouldFindSeatHoldByTripIdAndSeatNumber() {
        // Given
        SeatHold hold = SeatHold.builder()
                .seatNumber("B5")
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .trip(trip)
                .account(account)
                .build();
        seatHoldRepository.save(hold);

        // When
        Optional<SeatHold> found = seatHoldRepository.findByTrip_IdAndSeatNumber(trip.getId(), "B5");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getSeatNumber()).isEqualTo("B5");
    }

    @Test
    @DisplayName("Should find expired seat holds")
    void shouldFindExpiredSeatHolds() {
        // Given
        SeatHold expiredHold = SeatHold.builder()
                .seatNumber("C1")
                .expiresAt(LocalDateTime.now().minusHours(1))
                .trip(trip)
                .account(account)
                .build();

        SeatHold activeHold = SeatHold.builder()
                .seatNumber("C2")
                .expiresAt(LocalDateTime.now().plusHours(1))
                .trip(trip)
                .account(account)
                .build();

        seatHoldRepository.save(expiredHold);
        seatHoldRepository.save(activeHold);

        // When
        List<SeatHold> expired = seatHoldRepository.findByExpiresAtBefore(LocalDateTime.now());

        // Then
        assertThat(expired).hasSize(1);
        assertThat(expired.get(0).getSeatNumber()).isEqualTo("C1");
    }

    @Test
    @DisplayName("Should find seat holds by account ID")
    void shouldFindSeatHoldsByAccountId() {
        // Given
        SeatHold hold1 = SeatHold.builder()
                .seatNumber("D1")
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .trip(trip)
                .account(account)
                .build();

        SeatHold hold2 = SeatHold.builder()
                .seatNumber("D2")
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .trip(trip)
                .account(account)
                .build();

        seatHoldRepository.save(hold1);
        seatHoldRepository.save(hold2);

        // When
        List<SeatHold> holds = seatHoldRepository.findByAccount_Id(account.getId());

        // Then
        assertThat(holds).hasSize(2);
    }
}
