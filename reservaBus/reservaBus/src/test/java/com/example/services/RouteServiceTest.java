package com.example.services;

import com.example.api.dto.RouteDTOs;
import com.example.api.dto.StopDTOs;
import com.example.domain.entities.Route;
import com.example.domain.entities.Stop;
import com.example.domain.repositories.RouteRepository;
import com.example.domain.repositories.StopRepository;
import com.example.exceptions.NotFoundException;
import com.example.services.definitions.RouteServiceImpl;
import com.example.services.mappers.RouteMapper;
import com.example.services.mappers.StopMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Route Service Unit Tests")
class RouteServiceTest {

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private StopRepository stopRepository;

    @Mock
    private RouteMapper routeMapper;

    @Mock
    private StopMapper stopMapper;

    @InjectMocks
    private RouteServiceImpl routeService;

    private Route route;
    private RouteDTOs.RouteResponse routeResponse;
    private RouteDTOs.CreateRouteRequest createRequest;
    private RouteDTOs.UpdateRouteRequest updateRequest;

    @BeforeEach
    void setUp() {
        route = Route.builder()
                .id(1L)
                .code("RT001")
                .name("Test Route")
                .origin("Lima")
                .destination("Cusco")
                .durationMinutes(120)
                .distanceKm(100.0)
                .pricePerKm(0.5)
                .build();

        routeResponse = new RouteDTOs.RouteResponse(1L, "RT001", "Test Route", "Lima", "Cusco", 120, 100.0, 0.5);
        createRequest = new RouteDTOs.CreateRouteRequest("RT001", "Test Route", "Lima", "Cusco", 120, 100.0, 0.5);
        updateRequest = new RouteDTOs.UpdateRouteRequest(
                Optional.of("RT002"),
                Optional.of("Updated Route"),
                Optional.of("Arequipa"),
                Optional.of("Puno"),
                Optional.of(180),
                Optional.of(150.0),
                Optional.of(0.6));
    }

    @Test
    @DisplayName("Should create route successfully")
    void shouldCreateRoute() {
        // Given
        when(routeMapper.toEntity(createRequest)).thenReturn(route);
        when(routeRepository.save(route)).thenReturn(route);
        when(routeMapper.toResponse(route)).thenReturn(routeResponse);

        // When
        RouteDTOs.RouteResponse result = routeService.createRoute(createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        verify(routeRepository).save(route);
    }

    @Test
    @DisplayName("Should get route by ID successfully")
    void shouldGetRouteById() {
        // Given
        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));
        when(routeMapper.toResponse(route)).thenReturn(routeResponse);

        // When
        RouteDTOs.RouteResponse result = routeService.getRouteById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.origin()).isEqualTo("Lima");
        verify(routeRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw NotFoundException when route not found by ID")
    void shouldThrowNotFoundExceptionWhenRouteNotFoundById() {
        // Given
        when(routeRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> routeService.getRouteById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Route 999 not found");
    }

    @Test
    @DisplayName("Should update route successfully")
    void shouldUpdateRoute() {
        // Given
        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));
        when(routeRepository.save(route)).thenReturn(route);
        when(routeMapper.toResponse(route)).thenReturn(routeResponse);

        // When
        RouteDTOs.RouteResponse result = routeService.updateRoute(1L, updateRequest);

        // Then
        assertThat(result).isNotNull();
        verify(routeRepository).save(route);
    }

    @Test
    @DisplayName("Should delete route successfully")
    void shouldDeleteRoute() {
        // Given
        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));

        // When
        routeService.deleteRoute(1L);

        // Then
        verify(routeRepository).delete(route);
    }

    @Test
    @DisplayName("Should find routes by origin")
    void shouldFindRoutesByOrigin() {
        // Given
        List<Route> routes = Arrays.asList(route);
        when(routeRepository.findByOrigin("Lima")).thenReturn(routes);
        when(routeMapper.toResponse(route)).thenReturn(routeResponse);

        // When
        List<RouteDTOs.RouteResponse> result = routeService.findByOrigin("Lima");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).origin()).isEqualTo("Lima");
    }

    @Test
    @DisplayName("Should find routes by destination")
    void shouldFindRoutesByDestination() {
        // Given
        List<Route> routes = Arrays.asList(route);
        when(routeRepository.findByDestination("Cusco")).thenReturn(routes);
        when(routeMapper.toResponse(route)).thenReturn(routeResponse);

        // When
        List<RouteDTOs.RouteResponse> result = routeService.findByDestination("Cusco");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).destination()).isEqualTo("Cusco");
    }

    @Test
    @DisplayName("Should find routes by origin and destination")
    void shouldFindRoutesByOriginAndDestination() {
        // Given
        List<Route> routes = Arrays.asList(route);
        when(routeRepository.findByOriginAndDestination("Lima", "Cusco")).thenReturn(routes);
        when(routeMapper.toResponse(route)).thenReturn(routeResponse);

        // When
        List<RouteDTOs.RouteResponse> result = routeService.findByOriginAndDestination("Lima", "Cusco");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).origin()).isEqualTo("Lima");
        assertThat(result.get(0).destination()).isEqualTo("Cusco");
    }

    @Test
    @DisplayName("Should get stops by route ID")
    void shouldGetStopsByRouteId() {
        // Given
        Stop stop = Stop.builder().id(1L).name("Stop 1").route(route).build();
        StopDTOs.StopResponse stopResponse = new StopDTOs.StopResponse(1L, "Stop 1", 1, -12.0, -77.0, 1L);

        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));
        when(stopRepository.findByRoute_IdOrderBySequenceAsc(1L)).thenReturn(Arrays.asList(stop));
        when(stopMapper.toResponse(stop)).thenReturn(stopResponse);

        // When
        List<StopDTOs.StopResponse> result = routeService.getStopsByRouteId(1L);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Stop 1");
    }
}
