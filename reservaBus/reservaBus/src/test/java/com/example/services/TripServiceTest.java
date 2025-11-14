package com.example.services;

import com.example.api.dto.TripDTOs;
import com.example.domain.entities.Bus;
import com.example.domain.entities.Route;
import com.example.domain.entities.Trip;
import com.example.domain.repositories.BusRepository;
import com.example.domain.repositories.RouteRepository;
import com.example.domain.repositories.TripRepository;
import com.example.exceptions.NotFoundException;
import com.example.services.definitions.TripServiceImpl;
import com.example.services.mappers.TripMapper;
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
@DisplayName("Trip Service Unit Tests")
class TripServiceTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private TripMapper tripMapper;

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private BusRepository busRepository;

    @InjectMocks
    private TripServiceImpl tripService;

    private Trip trip;
    private Route route;
    private Bus bus;
    private TripDTOs.TripResponse tripResponse;
    private TripDTOs.CreateTripRequest createRequest;
    private TripDTOs.UpdateTripRequest updateRequest;

    @BeforeEach
    void setUp() {
        route = Route.builder().id(1L).name("Test Route").build();
        bus = Bus.builder().id(1L).plate("ABC123").build();

        trip = Trip.builder()
                .id(1L)
                .route(route)
                .bus(bus)
                .build();

        tripResponse = new TripDTOs.TripResponse(1L, 1L, 1L, LocalDateTime.now(), LocalDateTime.now().plusHours(2));
        createRequest = new TripDTOs.CreateTripRequest(1L, 1L, LocalDateTime.now(), LocalDateTime.now().plusHours(2));
        updateRequest = new TripDTOs.UpdateTripRequest(1L, 1L, LocalDateTime.now(), LocalDateTime.now().plusHours(2));
    }

    @Test
    @DisplayName("Should create trip successfully")
    void shouldCreateTrip() {
        // Given
        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));
        when(busRepository.findById(1L)).thenReturn(Optional.of(bus));
        when(tripRepository.save(any(Trip.class))).thenReturn(trip);
        when(tripMapper.toResponse(trip)).thenReturn(tripResponse);

        // When
        TripDTOs.TripResponse result = tripService.createTrip(createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        verify(tripRepository).save(any(Trip.class));
    }

    @Test
    @DisplayName("Should get trip by ID successfully")
    void shouldGetTripById() {
        // Given
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(tripMapper.toResponse(trip)).thenReturn(tripResponse);

        // When
        TripDTOs.TripResponse result = tripService.getTripById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.routeId()).isEqualTo(1L);
        verify(tripRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw NotFoundException when trip not found by ID")
    void shouldThrowNotFoundExceptionWhenTripNotFoundById() {
        // Given
        when(tripRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> tripService.getTripById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Trip 999 not found");
    }

    @Test
    @DisplayName("Should update trip successfully")
    void shouldUpdateTrip() {
        // Given
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));
        when(busRepository.findById(1L)).thenReturn(Optional.of(bus));
        when(tripRepository.save(trip)).thenReturn(trip);
        when(tripMapper.toResponse(trip)).thenReturn(tripResponse);

        // When
        TripDTOs.TripResponse result = tripService.updateTrip(1L, updateRequest);

        // Then
        assertThat(result).isNotNull();
        verify(tripMapper).patch(trip, updateRequest);
        verify(tripRepository).save(trip);
    }

    @Test
    @DisplayName("Should delete trip successfully")
    void shouldDeleteTrip() {
        // Given
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));

        // When
        tripService.deleteTrip(1L);

        // Then
        verify(tripRepository).delete(trip);
    }

    @Test
    @DisplayName("Should throw NotFoundException when deleting non-existent trip")
    void shouldThrowNotFoundExceptionWhenDeletingNonExistentTrip() {
        // Given
        when(tripRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> tripService.deleteTrip(999L))
                .isInstanceOf(NotFoundException.class);
    }
}
