package com.example.domain.repositories;

import com.example.domain.entities.Account;
import com.example.domain.entities.Baggage;
import com.example.domain.entities.Bus;
import com.example.domain.entities.Route;
import com.example.domain.entities.Stop;
import com.example.domain.entities.Ticket;
import com.example.domain.entities.Trip;
import com.example.domain.enums.AccountRole;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestJpaConfiguration.class)
@DisplayName("Baggage Repository Integration Tests")
class BaggageRepositoryIT {

        @Container
        @ServiceConnection
        static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

        @Autowired
        private BaggageRepository baggageRepository;

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

        private Ticket ticket;

        @BeforeEach
        void setUp() {
                // Create account
                Account account = Account.builder()
                                .email("passenger@test.com")
                                .passwordHash("$2a$10$hashedpassword")
                                .name("John Doe")
                                .phone("123456789")
                                .role(AccountRole.PASSENGER)
                                .status(com.example.domain.enums.AccountStatus.ACTIVE)
                                .build();
                account = accountRepository.save(account);

                // Create route
                Route route = Route.builder()
                                .code("RT001")
                                .name("Test Route")
                                .origin("City A")
                                .destination("City B")
                                .durationMinutes(120)
                                .distanceKm(100.0)
                                .pricePerKm(0.5)
                                .build();
                route = routeRepository.save(route);

                // Create stops
                Stop departureStop = Stop.builder()
                                .name("Stop A")
                                .latitude(-12.046373)
                                .longitude(-77.042754)
                                .route(route)
                                .sequence(1)
                                .build();
                departureStop = stopRepository.save(departureStop);

                Stop arrivalStop = Stop.builder()
                                .name("Stop B")
                                .latitude(-12.056373)
                                .longitude(-77.052754)
                                .route(route)
                                .sequence(2)
                                .build();
                arrivalStop = stopRepository.save(arrivalStop);

                // Create bus
                Bus bus = Bus.builder()
                                .plate("ABC123")
                                .capacity(40)
                                .status(BusStatus.ACTIVE)
                                .build();
                bus = busRepository.save(bus);

                // Create trip
                LocalDateTime departure = LocalDateTime.now().plusHours(2);
                Trip trip = Trip.builder()
                                .route(route)
                                .bus(bus)
                                .departureAt(departure)
                                .arrivalAt(departure.plusHours(3))
                                .status(com.example.domain.enums.TripStatus.SCHEDULED)
                                .build();
                trip = tripRepository.save(trip);

                // Create ticket
                ticket = Ticket.builder()
                                .qrCode("QR123")
                                .account(account)
                                .trip(trip)
                                .fromStop(departureStop)
                                .toStop(arrivalStop)
                                .seatNumber("10")
                                .price(50.0)
                                .status(TicketStatus.CONFIRMED)
                                .paymentMethod(PaymentMethod.CASH)
                                .build();
                ticket = ticketRepository.save(ticket);
        }

        @Test
        @DisplayName("Should save and retrieve baggage")
        void shouldSaveAndRetrieveBaggage() {
                // Given
                Baggage baggage = Baggage.builder()
                                .weight(15.5)
                                .fee(new BigDecimal("25.00"))
                                .tagCode("BAG001")
                                .ticket(ticket)
                                .build();

                // When
                Baggage saved = baggageRepository.save(baggage);

                // Then
                assertThat(saved.getId()).isNotNull();
                assertThat(saved.getWeight()).isEqualTo(15.5);
                assertThat(saved.getFee()).isEqualByComparingTo(new BigDecimal("25.00"));
                assertThat(saved.getTagCode()).isEqualTo("BAG001");
        }

        @Test
        @DisplayName("Should find baggage by tag code")
        void shouldFindBaggageByTagCode() {
                // Given
                Baggage baggage = Baggage.builder()
                                .weight(20.0)
                                .fee(new BigDecimal("30.00"))
                                .tagCode("BAG002")
                                .ticket(ticket)
                                .build();
                baggageRepository.save(baggage);

                // When
                Baggage found = baggageRepository.findByTagCode("BAG002");

                // Then
                assertThat(found).isNotNull();
                assertThat(found.getTagCode()).isEqualTo("BAG002");
                assertThat(found.getWeight()).isEqualTo(20.0);
        }

        @Test
        @DisplayName("Should find baggage by ticket ID")
        void shouldFindBaggageByTicketId() {
                // Given
                Baggage baggage1 = Baggage.builder()
                                .weight(10.0)
                                .fee(new BigDecimal("15.00"))
                                .tagCode("BAG003")
                                .ticket(ticket)
                                .build();

                Baggage baggage2 = Baggage.builder()
                                .weight(12.0)
                                .fee(new BigDecimal("20.00"))
                                .tagCode("BAG004")
                                .ticket(ticket)
                                .build();

                baggageRepository.save(baggage1);
                baggageRepository.save(baggage2);

                // When
                List<Baggage> baggages = baggageRepository.findByTicket_Id(ticket.getId());

                // Then
                assertThat(baggages).hasSize(2);
                assertThat(baggages).allMatch(b -> b.getTicket().getId().equals(ticket.getId()));
        }

        @Test
        @DisplayName("Should return empty list when ticket has no baggage")
        void shouldReturnEmptyListWhenTicketHasNoBaggage() {
                // When
                List<Baggage> baggages = baggageRepository.findByTicket_Id(999L);

                // Then
                assertThat(baggages).isEmpty();
        }
}
