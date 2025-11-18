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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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
                                .build();

                ticketResponse = new TicketDTOs.TicketResponse(1L, "A1", 1L, Optional.of(1L), Optional.of(2L),
                                PaymentMethod.CASH, "pi_123", "CONFIRMED", 50.0, "QR123");
                createRequest = new TicketDTOs.CreateTicketRequest("A1", 1L, Optional.of(1L), Optional.of(2L),
                                PaymentMethod.CASH, "pi_123",
                                FareRulePassengerType.ADULT);
                updateRequest = new TicketDTOs.UpdateTicketRequest("A2", 1L, 1L, 2L, FareRulePassengerType.CHILD);
        }

        @Test
        @DisplayName("Should create ticket successfully")
        void shouldCreateTicket() {
                // Given
                when(authenticationService.getCurrentAccount()).thenReturn(account);
                when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
                when(stopRepository.findById(1L)).thenReturn(Optional.of(fromStop));
                when(stopRepository.findById(2L)).thenReturn(Optional.of(toStop));
                when(ticketRepository.findTicketsSameTripAndStops(any(), any(), any()))
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
                when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
                when(stopRepository.findById(1L)).thenReturn(Optional.of(fromStop));
                when(stopRepository.findById(2L)).thenReturn(Optional.of(toStop));
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
}
