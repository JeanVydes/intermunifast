package com.example.services;

import com.example.api.dto.BusDTOs;
import com.example.domain.entities.Bus;
import com.example.domain.enums.BusStatus;
import com.example.domain.repositories.BusRepository;
import com.example.exceptions.NotFoundException;
import com.example.services.implementations.BusServiceImpl;
import com.example.services.mappers.BusMapper;
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
@DisplayName("Bus Service Unit Tests")
class BusServiceTest {

    @Mock
    private BusRepository busRepository;

    @Mock
    private BusMapper busMapper;

    @InjectMocks
    private BusServiceImpl busService;

    private Bus bus;
    private BusDTOs.BusResponse busResponse;
    private BusDTOs.CreateBusRequest createRequest;
    private BusDTOs.UpdateBusRequest updateRequest;

    @BeforeEach
    void setUp() {
        bus = Bus.builder()
                .id(1L)
                .plate("ABC123")
                .capacity(40)
                .status(BusStatus.ACTIVE)
                .build();

        busResponse = new BusDTOs.BusResponse(1L, "ABC123", 40, null, "ACTIVE");
        createRequest = new BusDTOs.CreateBusRequest("ABC123", 40, null);
        updateRequest = new BusDTOs.UpdateBusRequest(
                Optional.of("XYZ789"),
                Optional.of(45),
                Optional.empty(),
                Optional.of(BusStatus.MAINTENANCE));
    }

    @Test
    @DisplayName("Should create bus successfully")
    void shouldCreateBus() {
        // Given
        when(busMapper.toEntity(createRequest)).thenReturn(bus);
        when(busRepository.save(bus)).thenReturn(bus);
        when(busMapper.toResponse(bus)).thenReturn(busResponse);

        // When
        BusDTOs.BusResponse result = busService.createBus(createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        verify(busRepository).save(bus);
    }

    @Test
    @DisplayName("Should get bus by ID successfully")
    void shouldGetBusById() {
        // Given
        when(busRepository.findById(1L)).thenReturn(Optional.of(bus));
        when(busMapper.toResponse(bus)).thenReturn(busResponse);

        // When
        BusDTOs.BusResponse result = busService.getBusById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        verify(busRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw NotFoundException when bus not found by ID")
    void shouldThrowNotFoundExceptionWhenBusNotFoundById() {
        // Given
        when(busRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> busService.getBusById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Bus 999 not found");
    }

    @Test
    @DisplayName("Should update bus successfully")
    void shouldUpdateBus() {
        // Given
        when(busRepository.findById(1L)).thenReturn(Optional.of(bus));
        when(busRepository.save(bus)).thenReturn(bus);
        when(busMapper.toResponse(bus)).thenReturn(busResponse);

        // When
        BusDTOs.BusResponse result = busService.updateBus(1L, updateRequest);

        // Then
        assertThat(result).isNotNull();
        verify(busRepository).save(bus);
    }

    @Test
    @DisplayName("Should delete bus successfully")
    void shouldDeleteBus() {
        // Given
        when(busRepository.findById(1L)).thenReturn(Optional.of(bus));

        // When
        busService.deleteBus(1L);

        // Then
        verify(busRepository).delete(bus);
    }

    @Test
    @DisplayName("Should throw NotFoundException when deleting non-existent bus")
    void shouldThrowNotFoundExceptionWhenDeletingNonExistentBus() {
        // Given
        when(busRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> busService.deleteBus(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Bus 999 not found");
    }
}
