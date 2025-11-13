package com.example.domain.repositories;

import com.example.domain.entities.Bus;
import com.example.domain.entities.Seat;
import com.example.domain.enums.BusStatus;
import com.example.domain.enums.SeatType;
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
@DisplayName("Seat Repository Integration Tests")
class SeatRepositoryIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private BusRepository busRepository;

    private Bus bus;

    @BeforeEach
    void setUp() {
        bus = Bus.builder()
                .plate("ABC123")
                .capacity(40)
                .status(BusStatus.ACTIVE)
                .build();
        bus = busRepository.save(bus);
    }

    @Test
    @DisplayName("Should save and retrieve seat")
    void shouldSaveAndRetrieveSeat() {
        // Given
        Seat seat = Seat.builder()
                .number("A1")
                .type(SeatType.STANDARD)
                .bus(bus)
                .build();

        // When
        Seat saved = seatRepository.save(seat);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getNumber()).isEqualTo("A1");
        assertThat(saved.getType()).isEqualTo(SeatType.STANDARD);
        assertThat(saved.getBus().getId()).isEqualTo(bus.getId());
    }

    @Test
    @DisplayName("Should find seats by bus ID")
    void shouldFindSeatsByBusId() {
        // Given
        Seat seat1 = Seat.builder()
                .number("A1")
                .type(SeatType.STANDARD)
                .bus(bus)
                .build();

        Seat seat2 = Seat.builder()
                .number("A2")
                .type(SeatType.PREFERENTIAL)
                .bus(bus)
                .build();

        Seat seat3 = Seat.builder()
                .number("B1")
                .type(SeatType.STANDARD)
                .bus(bus)
                .build();

        seatRepository.save(seat1);
        seatRepository.save(seat2);
        seatRepository.save(seat3);

        // When
        List<Seat> seats = seatRepository.findByBus_Id(bus.getId());

        // Then
        assertThat(seats).hasSize(3);
        assertThat(seats).extracting(Seat::getNumber)
                .containsExactlyInAnyOrder("A1", "A2", "B1");
    }

    @Test
    @DisplayName("Should return empty list when no seats for bus")
    void shouldReturnEmptyListWhenNoSeatsForBus() {
        // When
        List<Seat> seats = seatRepository.findByBus_Id(999L);

        // Then
        assertThat(seats).isEmpty();
    }

    @Test
    @DisplayName("Should find seats of different types")
    void shouldFindSeatsOfDifferentTypes() {
        // Given
        Seat standardSeat = Seat.builder()
                .number("S1")
                .type(SeatType.STANDARD)
                .bus(bus)
                .build();

        Seat preferentialSeat = Seat.builder()
                .number("P1")
                .type(SeatType.PREFERENTIAL)
                .bus(bus)
                .build();

        seatRepository.save(standardSeat);
        seatRepository.save(preferentialSeat);

        // When
        List<Seat> allSeats = seatRepository.findByBus_Id(bus.getId());

        // Then
        assertThat(allSeats).hasSize(2);
        assertThat(allSeats).anyMatch(s -> s.getType() == SeatType.STANDARD);
        assertThat(allSeats).anyMatch(s -> s.getType() == SeatType.PREFERENTIAL);
    }
}
