package com.example.domain.repositories;

import com.example.domain.entities.*;
import com.example.domain.enums.AccountRole;
import com.example.domain.enums.AccountStatus;
import com.example.domain.enums.BusStatus;
import com.example.domain.enums.PaymentMethod;
import com.example.domain.enums.TicketStatus;
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
@DisplayName("Ticket Repository Integration Tests")
class TicketRepositoryIT {

        @Container
        @ServiceConnection
        static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

        @Autowired
        private TicketRepository ticketRepository;

        @Autowired
        private AccountRepository accountRepository;

        @Autowired
        private TripRepository tripRepository;

        @Autowired
        private RouteRepository routeRepository;

        @Autowired
        private BusRepository busRepository;

        @Autowired
        private StopRepository stopRepository;

        private Account account;
        private Trip trip;
        private Stop fromStop;
        private Stop toStop;

        @BeforeEach
        void setUp() {
                account = Account.builder()
                                .name("Test Customer")
                                .email("customer@test.com")
                                .phone("1234567890")
                                .passwordHash("hash123")
                                .role(AccountRole.PASSENGER)
                                .status(AccountStatus.ACTIVE)
                                .build();
                account = accountRepository.save(account);

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

                Bus bus = Bus.builder()
                                .plate("BUS001")
                                .capacity(40)
                                .status(BusStatus.ACTIVE)
                                .build();
                bus = busRepository.save(bus);

                LocalDateTime departure = LocalDateTime.now().plusHours(2);
                trip = Trip.builder()
                                .departureAt(departure)
                                .arrivalAt(departure.plusHours(3))
                                .route(route)
                                .bus(bus)
                                .status(com.example.domain.enums.TripStatus.SCHEDULED)
                                .build();
                trip = tripRepository.save(trip);

                fromStop = Stop.builder()
                                .name("Stop A")
                                .sequence(1)
                                .latitude(10.0)
                                .longitude(20.0)
                                .route(route)
                                .build();
                fromStop = stopRepository.save(fromStop);

                toStop = Stop.builder()
                                .name("Stop B")
                                .sequence(2)
                                .latitude(11.0)
                                .longitude(21.0)
                                .route(route)
                                .build();
                toStop = stopRepository.save(toStop);
        }

        @Test
        @DisplayName("Should save and find ticket")
        void shouldSaveAndFindTicket() {
                // Given
                Ticket ticket = Ticket.builder()
                                .account(account)
                                .trip(trip)
                                .fromStop(fromStop)
                                .toStop(toStop)
                                .seatNumber("A1")
                                .price(50.0)
                                .qrCode("QR12345")
                                .status(TicketStatus.CONFIRMED)
                                .paymentMethod(PaymentMethod.CARD)
                                .build();

                // When
                Ticket saved = ticketRepository.save(ticket);

                // Then
                assertThat(saved.getId()).isNotNull();
                assertThat(saved.getSeatNumber()).isEqualTo("A1");
                assertThat(saved.getPrice()).isEqualTo(50.0);
                assertThat(saved.getStatus()).isEqualTo(TicketStatus.CONFIRMED);
        }

        @Test
        @DisplayName("Should find tickets by trip ID")
        void shouldFindTicketsByTripId() {
                // Given
                Ticket ticket = Ticket.builder()
                                .account(account)
                                .trip(trip)
                                .fromStop(fromStop)
                                .toStop(toStop)
                                .seatNumber("B2")
                                .price(60.0)
                                .qrCode("QR54321")
                                .status(TicketStatus.CONFIRMED)
                                .paymentMethod(PaymentMethod.CASH)
                                .build();
                ticketRepository.save(ticket);

                // When
                List<Ticket> found = ticketRepository.findByTrip_IdAndStatus(trip.getId(), TicketStatus.CONFIRMED);

                // Then
                assertThat(found).hasSize(1);
                assertThat(found.get(0).getTrip().getId()).isEqualTo(trip.getId());
        }

        @Test
        @DisplayName("Should find tickets by account ID")
        void shouldFindTicketsByAccountId() {
                // Given
                Ticket ticket1 = Ticket.builder()
                                .account(account)
                                .trip(trip)
                                .fromStop(fromStop)
                                .toStop(toStop)
                                .seatNumber("C3")
                                .price(55.0)
                                .qrCode("QR11111")
                                .status(TicketStatus.CONFIRMED)
                                .paymentMethod(PaymentMethod.QR)
                                .build();
                ticketRepository.save(ticket1);

                // When
                List<Ticket> found = ticketRepository.findByAccount_Id(account.getId());

                // Then
                assertThat(found).hasSize(1);
                assertThat(found.get(0).getAccount().getId()).isEqualTo(account.getId());
        }

        @Test
        @DisplayName("Should find tickets by QR code")
        void shouldFindTicketsByQrCode() {
                // Given
                String qrCode = "QR_UNIQUE_123";
                Ticket ticket = Ticket.builder()
                                .account(account)
                                .trip(trip)
                                .fromStop(fromStop)
                                .toStop(toStop)
                                .seatNumber("D4")
                                .price(65.0)
                                .qrCode(qrCode)
                                .status(TicketStatus.CONFIRMED)
                                .paymentMethod(PaymentMethod.TRANSFER)
                                .build();
                ticketRepository.save(ticket);

                // When
                Ticket found = ticketRepository.findByQrCode(qrCode);

                // Then
                assertThat(found).isNotNull();
                assertThat(found.getQrCode()).isEqualTo(qrCode);
        }
}
