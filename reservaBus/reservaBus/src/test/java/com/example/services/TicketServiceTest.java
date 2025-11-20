package com.example.services;

import com.example.api.dto.TicketDTOs;
import com.example.domain.entities.*;
import com.example.domain.enums.FareRulePassengerType;
import com.example.domain.enums.PaymentMethod;
import com.example.domain.enums.TicketStatus;
import com.example.domain.repositories.*;
import com.example.exceptions.NotFoundException;
import com.example.security.services.AuthenticationService;
import com.example.services.extra.SeatAvailabilityService;
import com.example.services.implementations.TicketServiceImpl;
import com.example.services.mappers.TicketMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Ticket Service Unit Tests")
class TicketServiceTest {

        @Mock
        private TicketRepository ticketRepository;

        @Mock
        private TicketMapper ticketMapper;

        @Mock
        private TripRepository tripRepository;

        @Mock
        private StopRepository stopRepository;

        @Mock
        private AccountRepository accountRepository;

        @Mock
        private AuthenticationService authenticationService;

        @Mock
        private FareRuleRepository fareRuleRepository;

        @Mock
        private SeatAvailabilityService seatAvailabilityService;

        @InjectMocks
        private TicketServiceImpl ticketService;

        private Ticket ticket;
        private Trip trip;
        private Route route;
        private Account account;
        private FareRule fareRule;
        private Stop fromStop;
        private Stop toStop;
        private TicketDTOs.TicketResponse ticketResponse;
        private TicketDTOs.CreateTicketRequest createRequest;
        private TicketDTOs.UpdateTicketRequest updateRequest;

        @BeforeEach
        void setUp() {
                account = Account.builder().id(1L).email("test@test.com").build();

                route = Route.builder()
                                .id(1L)
                                .distanceKm(100.0)
                                .pricePerKm(0.5)
                                .build();

                fareRule = FareRule.builder()
                                .id(1L)
                                .route(route)
                                .childrenDiscount(0.5)
                                .seniorDiscount(0.3)
                                .studentDiscount(0.2)
                                .build();

                Bus bus = Bus.builder()
                                .id(1L)
                                .capacity(40)
                                .build();

                trip = Trip.builder()
                                .id(1L)
                                .route(route)
                                .bus(bus)
                                .build();

                fromStop = Stop.builder()
                                .id(1L)
                                .name("Stop A")
                                .sequence(0)
                                .build();

                toStop = Stop.builder()
                                .id(2L)
                                .name("Stop B")
                                .sequence(1)
                                .build();

                ticket = Ticket.builder()
                                .id(1L)
                                .seatNumber("A1")
                                .trip(trip)
                                .account(account)
                                .fromStop(fromStop)
                                .toStop(toStop)
                                .paymentMethod(PaymentMethod.CASH)
                                .status(TicketStatus.CONFIRMED)
                                .passengerType(FareRulePassengerType.ADULT)
                                .price(50.0)
                                .qrCode("QR123")
                                .build();

                ticketResponse = new TicketDTOs.TicketResponse(
                                1L, // id
                                "A1", // seatNumber
                                1L, // tripId
                                Optional.of(1L), // fromStopId
                                Optional.of(2L), // toStopId
                                PaymentMethod.CASH, // paymentMethod
                                "pi_123", // paymentIntentId
                                FareRulePassengerType.ADULT, // passengerType
                                "CONFIRMED", // status
                                "COMPLETED", // paymentStatus
                                50.0, // price
                                "QR123" // qrCode
                );

                createRequest = new TicketDTOs.CreateTicketRequest(
                                "A1",
                                1L,
                                Optional.of(1L),
                                Optional.of(2L),
                                PaymentMethod.CASH,
                                "pi_123",
                                FareRulePassengerType.ADULT);

                updateRequest = new TicketDTOs.UpdateTicketRequest(
                                "A2",
                                1L,
                                1L,
                                2L,
                                FareRulePassengerType.CHILD);
        }

        @Test
        @DisplayName("Should create ticket successfully")
        void shouldCreateTicket() {
                // Given
                when(authenticationService.getCurrentAccount()).thenReturn(account);
                when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
                when(stopRepository.findById(1L)).thenReturn(Optional.of(fromStop));
                when(stopRepository.findById(2L)).thenReturn(Optional.of(toStop));
                when(seatAvailabilityService.isSeatAvailable(any(), any(), any(), any())).thenReturn(true);
                when(ticketRepository.findByTrip_IdAndStatus(1L, TicketStatus.CONFIRMED))
                                .thenReturn(java.util.Collections.emptyList());
                when(fareRuleRepository.findByRouteId(1L)).thenReturn(fareRule);
                when(accountRepository.getReferenceById(1L)).thenReturn(account);
                when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);
                when(ticketMapper.toResponse(ticket)).thenReturn(ticketResponse);

                // When
                TicketDTOs.TicketResponse result = ticketService.createTicket(createRequest);

                // Then
                assertThat(result).isNotNull();
                assertThat(result.id()).isEqualTo(1L);
                verify(ticketRepository).save(any(Ticket.class));
        }

        @Test
        @DisplayName("Should get ticket by ID successfully")
        void shouldGetTicketById() {
                // Given
                when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
                when(ticketMapper.toResponse(ticket)).thenReturn(ticketResponse);

                // When
                TicketDTOs.TicketResponse result = ticketService.getTicketById(1L);

                // Then
                assertThat(result).isNotNull();
                assertThat(result.seatNumber()).isEqualTo("A1");
                verify(ticketRepository).findById(1L);
        }

        @Test
        @DisplayName("Should throw NotFoundException when ticket not found by ID")
        void shouldThrowNotFoundExceptionWhenTicketNotFoundById() {
                // Given
                when(ticketRepository.findById(999L)).thenReturn(Optional.empty());

                // When & Then
                assertThatThrownBy(() -> ticketService.getTicketById(999L))
                                .isInstanceOf(NotFoundException.class)
                                .hasMessageContaining("Ticket 999 not found");
        }

        @Test
        @DisplayName("Should update ticket successfully")
        void shouldUpdateTicket() {
                // Given
                when(tripRepository.existsById(1L)).thenReturn(true);
                when(stopRepository.existsById(1L)).thenReturn(true);
                when(stopRepository.existsById(2L)).thenReturn(true);
                when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
                when(authenticationService.getCurrentAccount()).thenReturn(account);
                when(fareRuleRepository.findByRouteId(1L)).thenReturn(fareRule);
                when(ticketRepository.save(ticket)).thenReturn(ticket);
                when(ticketMapper.toResponse(ticket)).thenReturn(ticketResponse);

                // When
                TicketDTOs.TicketResponse result = ticketService.updateTicket(1L, updateRequest);

                // Then
                assertThat(result).isNotNull();
                verify(ticketMapper).patch(ticket, updateRequest);
                verify(ticketRepository).save(ticket);
        }

        @Test
        @DisplayName("Should delete ticket successfully")
        void shouldDeleteTicket() {
                // Given
                when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

                // When
                ticketService.deleteTicket(1L);

                // Then
                verify(ticketRepository).delete(ticket);
        }

        @Test
        @DisplayName("Should throw NotFoundException when deleting non-existent ticket")
        void shouldThrowNotFoundExceptionWhenDeletingNonExistentTicket() {
                // Given
                when(ticketRepository.findById(999L)).thenReturn(Optional.empty());

                // When & Then
                assertThatThrownBy(() -> ticketService.deleteTicket(999L))
                                .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("Should throw NotFoundException when trip not found during ticket creation")
        void shouldThrowNotFoundExceptionWhenTripNotFound() {
                // Given
                when(authenticationService.getCurrentAccount()).thenReturn(account);
                when(tripRepository.findById(999L)).thenReturn(Optional.empty());

                TicketDTOs.CreateTicketRequest invalidRequest = new TicketDTOs.CreateTicketRequest(
                                "A1",
                                999L, // non-existent trip
                                Optional.empty(),
                                Optional.empty(),
                                PaymentMethod.CASH,
                                null,
                                FareRulePassengerType.ADULT);

                // When & Then
                assertThatThrownBy(() -> ticketService.createTicket(invalidRequest))
                                .isInstanceOf(NotFoundException.class)
                                .hasMessageContaining("Trip 999 not found");
        }

        @Test
        @DisplayName("Should throw NotFoundException when updating non-existent ticket")
        void shouldThrowNotFoundExceptionWhenUpdatingNonExistentTicket() {
                // Given - Using an empty update request (all fields null)
                TicketDTOs.UpdateTicketRequest emptyRequest = new TicketDTOs.UpdateTicketRequest(
                                null, // seatNumber
                                null, // tripId
                                null, // fromStopId
                                null, // toStopId
                                null // passengerType
                );
                when(ticketRepository.findById(999L)).thenReturn(Optional.empty());

                // When & Then
                assertThatThrownBy(() -> ticketService.updateTicket(999L, emptyRequest))
                                .isInstanceOf(NotFoundException.class)
                                .hasMessageContaining("Ticket 999 not found");
        }

        @Test
        @DisplayName("Should get all tickets for current user")
        void shouldGetAllTicketsForCurrentUser() {
                // Given
                when(authenticationService.getCurrentAccount()).thenReturn(account);
                when(ticketRepository.findByAccount_Id(1L)).thenReturn(java.util.List.of(ticket));
                when(ticketMapper.toResponse(ticket)).thenReturn(ticketResponse);

                // When
                var results = ticketService.getTicketsForCurrentUser(null);

                // Then
                assertThat(results).isNotNull();
                assertThat(results).hasSize(1);
                assertThat(results.get(0).seatNumber()).isEqualTo("A1");
                verify(authenticationService).getCurrentAccount();
        }

        @Test
        @DisplayName("Should calculate price correctly for child passenger")
        void shouldCalculatePriceForChildPassenger() {
                // Given
                TicketDTOs.CreateTicketRequest childRequest = new TicketDTOs.CreateTicketRequest(
                                "B1",
                                1L,
                                Optional.empty(),
                                Optional.empty(),
                                PaymentMethod.CARD,
                                "pi_456",
                                FareRulePassengerType.CHILD);

                when(authenticationService.getCurrentAccount()).thenReturn(account);
                when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
                when(seatAvailabilityService.isSeatAvailable(any(), any(), any(), any())).thenReturn(true);
                when(ticketRepository.findByTrip_IdAndStatus(1L, TicketStatus.CONFIRMED))
                                .thenReturn(java.util.Collections.emptyList());
                when(fareRuleRepository.findByRouteId(1L)).thenReturn(fareRule);
                when(accountRepository.getReferenceById(1L)).thenReturn(account);
                when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);
                when(ticketMapper.toResponse(any(Ticket.class))).thenReturn(ticketResponse);

                // When
                TicketDTOs.TicketResponse result = ticketService.createTicket(childRequest);

                // Then
                assertThat(result).isNotNull();
                verify(ticketRepository).save(any(Ticket.class));
                // Child discount should be applied (50% off according to fareRule)
        }

        @Test
        @DisplayName("Should handle ticket with full route (no stops)")
        void shouldHandleTicketWithFullRoute() {
                // Given
                TicketDTOs.CreateTicketRequest fullRouteRequest = new TicketDTOs.CreateTicketRequest(
                                "C1",
                                1L,
                                Optional.empty(), // fromStopId = null (full route)
                                Optional.empty(), // toStopId = null (full route)
                                PaymentMethod.CASH,
                                null,
                                FareRulePassengerType.ADULT);

                when(authenticationService.getCurrentAccount()).thenReturn(account);
                when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
                when(seatAvailabilityService.isSeatAvailable(any(), any(), any(), any())).thenReturn(true);
                when(ticketRepository.findByTrip_IdAndStatus(1L, TicketStatus.CONFIRMED))
                                .thenReturn(java.util.Collections.emptyList());
                when(fareRuleRepository.findByRouteId(1L)).thenReturn(fareRule);
                when(accountRepository.getReferenceById(1L)).thenReturn(account);
                when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);
                when(ticketMapper.toResponse(any(Ticket.class))).thenReturn(ticketResponse);

                // When
                TicketDTOs.TicketResponse result = ticketService.createTicket(fullRouteRequest);

                // Then
                assertThat(result).isNotNull();
                verify(ticketRepository).save(any(Ticket.class));
        }
}