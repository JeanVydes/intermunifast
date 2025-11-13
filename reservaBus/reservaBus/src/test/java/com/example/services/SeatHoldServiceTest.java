package com.example.services;

import com.example.api.dto.SeatHoldDTOs;
import com.example.domain.entities.Account;
import com.example.domain.entities.SeatHold;
import com.example.domain.entities.Trip;
import com.example.domain.repositories.AccountRepository;
import com.example.domain.repositories.SeatHoldRepository;
import com.example.domain.repositories.TripRepository;
import com.example.exceptions.NotFoundException;
import com.example.services.definitions.SeatHoldServiceImpl;
import com.example.services.mappers.SeatHoldMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SeatHold Service Unit Tests")
class SeatHoldServiceTest {

    @Mock
    private SeatHoldRepository seatHoldRepository;

    @Mock
    private SeatHoldMapper seatHoldMapper;

    @Mock
    private TripRepository tripRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private SeatHoldServiceImpl seatHoldService;

    private SeatHold seatHold;
    private Trip trip;
    private Account account;
    private SeatHoldDTOs.SeatHoldResponse seatHoldResponse;
    private SeatHoldDTOs.CreateSeatHoldRequest createRequest;
    private SeatHoldDTOs.UpdateSeatHoldRequest updateRequest;

    @BeforeEach
    void setUp() {
        trip = Trip.builder().id(1L).build();
        account = Account.builder().id(1L).email("test@test.com").build();

        seatHold = SeatHold.builder()
                .id(1L)
                .seatNumber("A1")
                .trip(trip)
                .account(account)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .build();

        seatHoldResponse = new SeatHoldDTOs.SeatHoldResponse(1L, "A1", 1L);
        createRequest = new SeatHoldDTOs.CreateSeatHoldRequest("A1", 1L);
        updateRequest = new SeatHoldDTOs.UpdateSeatHoldRequest("A2", 1L);
    }

    @Test
    @DisplayName("Should create seat hold successfully")
    void shouldCreateSeatHold() {
        // Given
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(seatHoldMapper.toEntity(createRequest)).thenReturn(seatHold);
        when(seatHoldRepository.save(any(SeatHold.class))).thenReturn(seatHold);
        when(seatHoldMapper.toResponse(seatHold)).thenReturn(seatHoldResponse);

        // When
        SeatHoldDTOs.SeatHoldResponse result = seatHoldService.reserveSeat(createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        verify(seatHoldRepository).save(any(SeatHold.class));
    }

    @Test
    @DisplayName("Should get seat hold by ID successfully")
    void shouldGetSeatHoldById() {
        // Given
        when(seatHoldRepository.findById(1L)).thenReturn(Optional.of(seatHold));
        when(seatHoldMapper.toResponse(seatHold)).thenReturn(seatHoldResponse);

        // When
        SeatHoldDTOs.SeatHoldResponse result = seatHoldService.getSeatReserveById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.seatNumber()).isEqualTo("A1");
        verify(seatHoldRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw NotFoundException when seat hold not found by ID")
    void shouldThrowNotFoundExceptionWhenSeatHoldNotFoundById() {
        // Given
        when(seatHoldRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> seatHoldService.getSeatReserveById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("SeatHold 999 not found");
    }

    @Test
    @DisplayName("Should update seat hold successfully")
    void shouldUpdateSeatHold() {
        // Given
        when(seatHoldRepository.findById(1L)).thenReturn(Optional.of(seatHold));
        when(seatHoldRepository.save(seatHold)).thenReturn(seatHold);
        when(seatHoldMapper.toResponse(seatHold)).thenReturn(seatHoldResponse);

        // When
        SeatHoldDTOs.SeatHoldResponse result = seatHoldService.updateSeatReserve(1L, updateRequest);

        // Then
        assertThat(result).isNotNull();
        verify(seatHoldMapper).patch(seatHold, updateRequest);
        verify(seatHoldRepository).save(seatHold);
    }

    @Test
    @DisplayName("Should delete seat hold successfully")
    void shouldDeleteSeatHold() {
        // Given
        when(seatHoldRepository.findById(1L)).thenReturn(Optional.of(seatHold));

        // When
        seatHoldService.deleteSeatHold(1L);

        // Then
        verify(seatHoldRepository).delete(seatHold);
    }

    @Test
    @DisplayName("Should throw NotFoundException when deleting non-existent seat hold")
    void shouldThrowNotFoundExceptionWhenDeletingNonExistentSeatHold() {
        // Given
        when(seatHoldRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> seatHoldService.deleteSeatHold(999L))
                .isInstanceOf(NotFoundException.class);
    }
}
