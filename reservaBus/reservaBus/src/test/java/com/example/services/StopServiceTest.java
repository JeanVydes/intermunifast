package com.example.services;

import com.example.api.dto.StopDTOs;
import com.example.domain.entities.Route;
import com.example.domain.entities.Stop;
import com.example.domain.repositories.RouteRepository;
import com.example.domain.repositories.StopRepository;
import com.example.exceptions.NotFoundException;
import com.example.services.implementations.StopServiceImpl;
import com.example.services.mappers.StopMapper;
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
@DisplayName("Stop Service Unit Tests")
class StopServiceTest {

    @Mock
    private StopRepository stopRepository;

    @Mock
    private StopMapper stopMapper;

    @Mock
    private RouteRepository routeRepository;

    @InjectMocks
    private StopServiceImpl stopService;

    private Stop stop;
    private Route route;
    private StopDTOs.StopResponse stopResponse;
    private StopDTOs.CreateStopRequest createRequest;
    private StopDTOs.UpdateStopRequest updateRequest;

    @BeforeEach
    void setUp() {
        route = Route.builder()
                .id(1L)
                .code("RT001")
                .name("Test Route")
                .build();

        stop = Stop.builder()
                .id(1L)
                .name("Stop A")
                .sequence(1)
                .latitude(-12.046373)
                .longitude(-77.042754)
                .route(route)
                .build();

        stopResponse = new StopDTOs.StopResponse(1L, "Stop A", 1, -12.046373, -77.042754, 1L);
        createRequest = new StopDTOs.CreateStopRequest("Stop A", 1, -12.046373, -77.042754, 1L);
        updateRequest = new StopDTOs.UpdateStopRequest(
                Optional.of("Stop B"),
                Optional.of(2),
                Optional.of(-12.056373),
                Optional.of(-77.052754),
                Optional.of(1L));
    }

    @Test
    @DisplayName("Should create stop successfully")
    void shouldCreateStop() {
        // Given
        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));
        when(stopRepository.save(any(Stop.class))).thenReturn(stop);
        when(stopMapper.toResponse(stop)).thenReturn(stopResponse);

        // When
        StopDTOs.StopResponse result = stopService.createStop(createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        verify(stopRepository).save(any(Stop.class));
    }

    @Test
    @DisplayName("Should throw NotFoundException when creating stop with non-existent route")
    void shouldThrowNotFoundExceptionWhenCreatingStopWithNonExistentRoute() {
        // Given
        when(routeRepository.findById(999L)).thenReturn(Optional.empty());
        StopDTOs.CreateStopRequest invalidRequest = new StopDTOs.CreateStopRequest("Stop A", 1, -12.046373, -77.042754,
                999L);

        // When & Then
        assertThatThrownBy(() -> stopService.createStop(invalidRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Route 999 not found");
    }

    @Test
    @DisplayName("Should get stop by ID successfully")
    void shouldGetStopById() {
        // Given
        when(stopRepository.findById(1L)).thenReturn(Optional.of(stop));
        when(stopMapper.toResponse(stop)).thenReturn(stopResponse);

        // When
        StopDTOs.StopResponse result = stopService.getStopById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Stop A");
        verify(stopRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw NotFoundException when stop not found by ID")
    void shouldThrowNotFoundExceptionWhenStopNotFoundById() {
        // Given
        when(stopRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> stopService.getStopById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Stop 999 not found");
    }

    @Test
    @DisplayName("Should update stop successfully")
    void shouldUpdateStop() {
        // Given
        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));
        when(stopRepository.findById(1L)).thenReturn(Optional.of(stop));
        when(stopRepository.save(stop)).thenReturn(stop);
        when(stopMapper.toResponse(stop)).thenReturn(stopResponse);

        // When
        StopDTOs.StopResponse result = stopService.updateStop(1L, updateRequest);

        // Then
        assertThat(result).isNotNull();
        verify(stopRepository).save(stop);
    }

    @Test
    @DisplayName("Should delete stop successfully")
    void shouldDeleteStop() {
        // Given
        when(stopRepository.findById(1L)).thenReturn(Optional.of(stop));

        // When
        stopService.deleteStop(1L);

        // Then
        verify(stopRepository).delete(stop);
    }

    @Test
    @DisplayName("Should throw NotFoundException when deleting non-existent stop")
    void shouldThrowNotFoundExceptionWhenDeletingNonExistentStop() {
        // Given
        when(stopRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> stopService.deleteStop(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Stop 999 not found");
    }
}
