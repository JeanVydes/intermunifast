package com.example.services;

import com.example.api.dto.BaggageDTOs;
import com.example.domain.entities.Baggage;
import com.example.domain.entities.Ticket;
import com.example.domain.repositories.BaggageRepository;
import com.example.domain.repositories.TicketRepository;
import com.example.exceptions.NotFoundException;
import com.example.services.implementations.BaggageServiceImpl;
import com.example.services.mappers.BaggageMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Baggage Service Unit Tests")
class BaggageServiceTest {

    @Mock
    private BaggageRepository baggageRepository;

    @Mock
    private BaggageMapper baggageMapper;

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private BaggageServiceImpl baggageService;

    private Baggage baggage;
    private Ticket ticket;
    private BaggageDTOs.BaggageResponse baggageResponse;
    private BaggageDTOs.CreateBaggageRequest createRequest;
    private BaggageDTOs.UpdateBaggageRequest updateRequest;

    @BeforeEach
    void setUp() {
        ticket = Ticket.builder().id(1L).seatNumber("A1").build();

        baggage = Baggage.builder()
                .id(1L)
                .weight(15.5)
                .fee(BigDecimal.valueOf(10.0))
                .tagCode("TAG123")
                .ticket(ticket)
                .build();

        baggageResponse = new BaggageDTOs.BaggageResponse(1L, 15.5, BigDecimal.valueOf(10.0), "TAG123", 1L);
        createRequest = new BaggageDTOs.CreateBaggageRequest(15.5, "TAG123", 1L);
        updateRequest = new BaggageDTOs.UpdateBaggageRequest(20.0, "TAG456", 1L);
    }

    @Test
    @DisplayName("Should create baggage successfully")
    void shouldCreateBaggage() {
        // Given
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(baggageRepository.save(any(Baggage.class))).thenReturn(baggage);
        when(baggageMapper.toResponse(baggage)).thenReturn(baggageResponse);

        // When
        BaggageDTOs.BaggageResponse result = baggageService.createBaggage(createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        verify(baggageRepository).save(any(Baggage.class));
    }

    @Test
    @DisplayName("Should get baggage by ID successfully")
    void shouldGetBaggageById() {
        // Given
        when(baggageRepository.findById(1L)).thenReturn(Optional.of(baggage));
        when(baggageMapper.toResponse(baggage)).thenReturn(baggageResponse);

        // When
        BaggageDTOs.BaggageResponse result = baggageService.getBaggageById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.tagCode()).isEqualTo("TAG123");
        verify(baggageRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw NotFoundException when baggage not found by ID")
    void shouldThrowNotFoundExceptionWhenBaggageNotFoundById() {
        // Given
        when(baggageRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> baggageService.getBaggageById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Baggage 999 not found");
    }

    @Test
    @DisplayName("Should update baggage successfully")
    void shouldUpdateBaggage() {
        // Given
        when(baggageRepository.findById(1L)).thenReturn(Optional.of(baggage));
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(baggageRepository.save(baggage)).thenReturn(baggage);
        when(baggageMapper.toResponse(baggage)).thenReturn(baggageResponse);

        // When
        BaggageDTOs.BaggageResponse result = baggageService.updateBaggage(1L, updateRequest);

        // Then
        assertThat(result).isNotNull();
        verify(baggageMapper).patch(baggage, updateRequest);
        verify(baggageRepository).save(baggage);
    }

    @Test
    @DisplayName("Should delete baggage successfully")
    void shouldDeleteBaggage() {
        // Given
        when(baggageRepository.findById(1L)).thenReturn(Optional.of(baggage));

        // When
        baggageService.deleteBaggage(1L);

        // Then
        verify(baggageRepository).delete(baggage);
    }

    @Test
    @DisplayName("Should throw NotFoundException when deleting non-existent baggage")
    void shouldThrowNotFoundExceptionWhenDeletingNonExistentBaggage() {
        // Given
        when(baggageRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> baggageService.deleteBaggage(999L))
                .isInstanceOf(NotFoundException.class);
    }
}
