package com.example.services;

import com.example.api.dto.SeatHoldDTOs;
import com.example.domain.entities.Account;
import com.example.domain.entities.SeatHold;
import com.example.domain.entities.Stop;
import com.example.domain.entities.Trip;
import com.example.domain.repositories.AccountRepository;
import com.example.domain.repositories.SeatHoldRepository;
import com.example.domain.repositories.StopRepository;
import com.example.domain.repositories.TripRepository;
import com.example.exceptions.NotFoundException;
import com.example.security.services.AuthenticationService;
import com.example.services.extra.SeatAvailabilityService;
import com.example.services.implementations.SeatHoldServiceImpl;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
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
        private StopRepository stopRepository;

        @Mock
        private AccountRepository accountRepository;

        @Mock
        private AuthenticationService authenticationService;

        @Mock
        private SeatAvailabilityService seatAvailabilityService;

        @InjectMocks
        private SeatHoldServiceImpl seatHoldService;

        private SeatHold seatHold;
        private Trip trip;
        private Account account;
        private Stop fromStop;
        private Stop toStop;
        private SeatHoldDTOs.SeatHoldResponse seatHoldResponse;
        private SeatHoldDTOs.CreateSeatHoldRequest createRequest;
        private SeatHoldDTOs.UpdateSeatHoldRequest updateRequest;

        @BeforeEach
        void setUp() {
                trip = Trip.builder().id(1L).build();
                account = Account.builder().id(1L).email("test@test.com").build();

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

                seatHold = SeatHold.builder()
                                .id(1L)
                                .seatNumber("A1")
                                .trip(trip)
                                .account(account)
                                .fromStop(fromStop)
                                .toStop(toStop)
                                .expiresAt(LocalDateTime.now().plusMinutes(15))
                                .build();

                seatHoldResponse = new SeatHoldDTOs.SeatHoldResponse(
                                1L,
                                "A1",
                                1L,
                                Optional.of(1L),
                                Optional.of(2L),
                                LocalDateTime.now().plusMinutes(15),
                                System.currentTimeMillis());

                createRequest = new SeatHoldDTOs.CreateSeatHoldRequest(
                                "A1",
                                1L,
                                Optional.of(1L),
                                Optional.of(2L),
                                LocalDateTime.now().plusMinutes(10));

                updateRequest = new SeatHoldDTOs.UpdateSeatHoldRequest(
                                Optional.of("A2"),
                                Optional.of(1L),
                                Optional.of(1L),
                                Optional.of(2L),
                                Optional.empty());
        }

        @Test
        @DisplayName("Should create seat hold successfully")
        void shouldCreateSeatHold() {
                // Given
                when(authenticationService.getCurrentAccountId()).thenReturn(1L);
                when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
                when(stopRepository.findById(1L)).thenReturn(Optional.of(fromStop));
                when(stopRepository.findById(2L)).thenReturn(Optional.of(toStop));
                when(seatAvailabilityService.isSeatAvailable(anyLong(), anyString(), any(Stop.class), any(Stop.class)))
                                .thenReturn(true);
                when(seatHoldMapper.toEntity(createRequest)).thenReturn(seatHold);
                when(seatHoldRepository.save(any(SeatHold.class))).thenReturn(seatHold);
                when(seatHoldMapper.toResponse(seatHold)).thenReturn(seatHoldResponse);
                when(accountRepository.getReferenceById(1L)).thenReturn(account);

                // When
                SeatHoldDTOs.SeatHoldResponse result = seatHoldService.reserveSeat(createRequest);

                // Then
                assertThat(result).isNotNull();
                assertThat(result.id()).isEqualTo(1L);
                verify(seatAvailabilityService).isSeatAvailable(anyLong(), anyString(), any(Stop.class),
                                any(Stop.class));
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
                when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
                when(stopRepository.findById(1L)).thenReturn(Optional.of(fromStop));
                when(stopRepository.findById(2L)).thenReturn(Optional.of(toStop));
                when(seatAvailabilityService.isSeatAvailableExcludingHold(
                                anyLong(), anyString(), any(Stop.class), any(Stop.class), anyLong()))
                                .thenReturn(true);
                when(seatHoldRepository.save(seatHold)).thenReturn(seatHold);
                when(seatHoldMapper.toResponse(seatHold)).thenReturn(seatHoldResponse);

                // When
                SeatHoldDTOs.SeatHoldResponse result = seatHoldService.updateSeatReserve(1L, updateRequest);

                // Then
                assertThat(result).isNotNull();
                verify(seatAvailabilityService).isSeatAvailableExcludingHold(
                                anyLong(), anyString(), any(Stop.class), any(Stop.class), anyLong());
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

        @Test
        @DisplayName("Should throw IllegalStateException when seat is not available")
        void shouldThrowIllegalStateExceptionWhenSeatNotAvailable() {
                // Given
                when(authenticationService.getCurrentAccountId()).thenReturn(1L);
                when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
                when(stopRepository.findById(1L)).thenReturn(Optional.of(fromStop));
                when(stopRepository.findById(2L)).thenReturn(Optional.of(toStop));
                when(seatAvailabilityService.isSeatAvailable(anyLong(), anyString(), any(Stop.class), any(Stop.class)))
                                .thenReturn(false);
                when(seatAvailabilityService.getAvailabilityConflictReason(anyLong(), anyString(), any(Stop.class),
                                any(Stop.class)))
                                .thenReturn("Seat already sold for overlapping route segment");

                // When & Then
                assertThatThrownBy(() -> seatHoldService.reserveSeat(createRequest))
                                .isInstanceOf(IllegalStateException.class)
                                .hasMessageContaining("is not available");
        }

        @Test
        @DisplayName("Should throw NotFoundException when trip not found on create")
        void shouldThrowNotFoundExceptionWhenTripNotFoundOnCreate() {
                // Given
                when(authenticationService.getCurrentAccountId()).thenReturn(1L);
                when(tripRepository.findById(1L)).thenReturn(Optional.empty());

                // When & Then
                assertThatThrownBy(() -> seatHoldService.reserveSeat(createRequest))
                                .isInstanceOf(NotFoundException.class)
                                .hasMessageContaining("Trip 1 not found");
        }

        @Test
        @DisplayName("Should throw NotFoundException when from stop not found on create")
        void shouldThrowNotFoundExceptionWhenFromStopNotFoundOnCreate() {
                // Given
                when(authenticationService.getCurrentAccountId()).thenReturn(1L);
                when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
                when(stopRepository.findById(1L)).thenReturn(Optional.empty());

                // When & Then
                assertThatThrownBy(() -> seatHoldService.reserveSeat(createRequest))
                                .isInstanceOf(NotFoundException.class)
                                .hasMessageContaining("From Stop 1 not found");
        }

        @Test
        @DisplayName("Should throw NotFoundException when to stop not found on create")
        void shouldThrowNotFoundExceptionWhenToStopNotFoundOnCreate() {
                // Given
                when(authenticationService.getCurrentAccountId()).thenReturn(1L);
                when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
                when(stopRepository.findById(1L)).thenReturn(Optional.of(fromStop));
                when(stopRepository.findById(2L)).thenReturn(Optional.empty());

                // When & Then
                assertThatThrownBy(() -> seatHoldService.reserveSeat(createRequest))
                                .isInstanceOf(NotFoundException.class)
                                .hasMessageContaining("To Stop 2 not found");
        }

        @Test
        @DisplayName("Should throw IllegalStateException when updating to unavailable seat")
        void shouldThrowIllegalStateExceptionWhenUpdatingToUnavailableSeat() {
                // Given
                when(seatHoldRepository.findById(1L)).thenReturn(Optional.of(seatHold));
                when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
                when(stopRepository.findById(1L)).thenReturn(Optional.of(fromStop));
                when(stopRepository.findById(2L)).thenReturn(Optional.of(toStop));
                when(seatAvailabilityService.isSeatAvailableExcludingHold(
                                anyLong(), anyString(), any(Stop.class), any(Stop.class), anyLong()))
                                .thenReturn(false);
                when(seatAvailabilityService.getAvailabilityConflictReason(
                                anyLong(), anyString(), any(Stop.class), any(Stop.class)))
                                .thenReturn("Seat already on hold");

                // When & Then
                assertThatThrownBy(() -> seatHoldService.updateSeatReserve(1L, updateRequest))
                                .isInstanceOf(IllegalStateException.class)
                                .hasMessageContaining("Cannot update hold");
        }
}
