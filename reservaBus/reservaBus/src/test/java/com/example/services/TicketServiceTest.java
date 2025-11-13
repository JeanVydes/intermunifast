package com.example.services;

import com.example.api.dto.TicketDTOs;
import com.example.domain.entities.Stop;
import com.example.domain.entities.Ticket;
import com.example.domain.entities.Trip;
import com.example.domain.enums.FareRulePassengerType;
import com.example.domain.enums.PaymentMethod;
import com.example.domain.enums.TicketStatus;
import com.example.domain.repositories.StopRepository;
import com.example.domain.repositories.TicketRepository;
import com.example.domain.repositories.TripRepository;
import com.example.exceptions.NotFoundException;
import com.example.services.definitions.TicketServiceImpl;
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

    @InjectMocks
    private TicketServiceImpl ticketService;

    private Ticket ticket;
    private Trip trip;
    private Stop fromStop;
    private Stop toStop;
    private TicketDTOs.TicketResponse ticketResponse;
    private TicketDTOs.CreateTicketRequest createRequest;
    private TicketDTOs.UpdateTicketRequest updateRequest;

    @BeforeEach
    void setUp() {
        trip = Trip.builder().id(1L).build();
        fromStop = Stop.builder().id(1L).name("Stop A").build();
        toStop = Stop.builder().id(2L).name("Stop B").build();

        ticket = Ticket.builder()
                .id(1L)
                .seatNumber("A1")
                .trip(trip)
                .fromStop(fromStop)
                .toStop(toStop)
                .paymentMethod(PaymentMethod.CASH)
                .status(TicketStatus.SOLD)
                .build();

        ticketResponse = new TicketDTOs.TicketResponse(1L, "A1", 1L, 1L, 2L, PaymentMethod.CASH, "pi_123");
        createRequest = new TicketDTOs.CreateTicketRequest("A1", 1L, 1L, 2L, PaymentMethod.CASH, "pi_123",
                FareRulePassengerType.ADULT);
        updateRequest = new TicketDTOs.UpdateTicketRequest("A2", 1L, 1L, 2L, FareRulePassengerType.CHILD);
    }

    @Test
    @DisplayName("Should create ticket successfully")
    void shouldCreateTicket() {
        // Given
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(stopRepository.findById(1L)).thenReturn(Optional.of(fromStop));
        when(stopRepository.findById(2L)).thenReturn(Optional.of(toStop));
        when(ticketMapper.toEntity(createRequest)).thenReturn(ticket);
        when(ticketRepository.save(ticket)).thenReturn(ticket);
        when(ticketMapper.toResponse(ticket)).thenReturn(ticketResponse);

        // When
        TicketDTOs.TicketResponse result = ticketService.createTicket(createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        verify(ticketRepository).save(ticket);
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
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
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
