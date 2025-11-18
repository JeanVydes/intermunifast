package com.example.services;

import com.example.api.dto.ParcelDTOs;
import com.example.domain.entities.Parcel;
import com.example.domain.entities.Stop;
import com.example.domain.enums.ParcelStatus;
import com.example.domain.repositories.ParcelRepository;
import com.example.domain.repositories.StopRepository;
import com.example.exceptions.NotFoundException;
import com.example.services.implementations.ParcelServiceImpl;
import com.example.services.mappers.ParcelMapper;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Parcel Service Unit Tests")
class ParcelServiceTest {

    @Mock
    private ParcelRepository parcelRepository;

    @Mock
    private ParcelMapper parcelMapper;

    @Mock
    private StopRepository stopRepository;

    @InjectMocks
    private ParcelServiceImpl parcelService;

    private Parcel parcel;
    private Stop fromStop;
    private Stop toStop;
    private ParcelDTOs.ParcelResponse parcelResponse;
    private ParcelDTOs.CreateParcelRequest createRequest;
    private ParcelDTOs.UpdateParcelRequest updateRequest;

    @BeforeEach
    void setUp() {
        fromStop = Stop.builder().id(1L).name("Stop A").build();
        toStop = Stop.builder().id(2L).name("Stop B").build();

        parcel = Parcel.builder()
                .id(1L)
                .code("PCL001")
                .senderName("John Doe")
                .senderPhone("123456789")
                .receiverName("Jane Doe")
                .receiverPhone("987654321")
                .fromStop(fromStop)
                .toStop(toStop)
                .status(ParcelStatus.CREATED)
                .price(50.0)
                .build();

        parcelResponse = new ParcelDTOs.ParcelResponse(1L, "PCL001", "John Doe", "123456789", "Jane Doe", "987654321",
                1L, 2L, "CREATED", null, "123456", 50.0);
        createRequest = new ParcelDTOs.CreateParcelRequest("PCL001", "John Doe", "123456789", "Jane Doe", "987654321",
                1L, 2L);
        updateRequest = new ParcelDTOs.UpdateParcelRequest("PCL002", "John Smith", "111222333", "Jane Smith",
                "444555666", 1L, 2L, "IN_TRANSIT", "https://example.com/proof.jpg", 60.0);
    }

    @Test
    @DisplayName("Should create parcel successfully")
    void shouldCreateParcel() {
        // Given
        when(stopRepository.findById(1L)).thenReturn(Optional.of(fromStop));
        when(stopRepository.findById(2L)).thenReturn(Optional.of(toStop));
        when(parcelRepository.save(any(Parcel.class))).thenReturn(parcel);
        when(parcelMapper.toResponse(parcel)).thenReturn(parcelResponse);

        // When
        ParcelDTOs.ParcelResponse result = parcelService.createParcel(createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        verify(parcelRepository).save(any(Parcel.class));
    }

    @Test
    @DisplayName("Should get parcel by ID successfully")
    void shouldGetParcelById() {
        // Given
        when(parcelRepository.findById(1L)).thenReturn(Optional.of(parcel));
        when(parcelMapper.toResponse(parcel)).thenReturn(parcelResponse);

        // When
        ParcelDTOs.ParcelResponse result = parcelService.getParcelById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.code()).isEqualTo("PCL001");
        verify(parcelRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw NotFoundException when parcel not found by ID")
    void shouldThrowNotFoundExceptionWhenParcelNotFoundById() {
        // Given
        when(parcelRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> parcelService.getParcelById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Parcel 999 not found");
    }

    @Test
    @DisplayName("Should update parcel successfully")
    void shouldUpdateParcel() {
        // Given
        when(parcelRepository.findById(1L)).thenReturn(Optional.of(parcel));
        when(stopRepository.findById(1L)).thenReturn(Optional.of(fromStop));
        when(stopRepository.findById(2L)).thenReturn(Optional.of(toStop));
        when(parcelRepository.save(parcel)).thenReturn(parcel);
        when(parcelMapper.toResponse(parcel)).thenReturn(parcelResponse);

        // When
        ParcelDTOs.ParcelResponse result = parcelService.updateParcel(1L, updateRequest);

        // Then
        assertThat(result).isNotNull();
        verify(parcelMapper).patch(parcel, updateRequest);
        verify(parcelRepository).save(parcel);
    }

    @Test
    @DisplayName("Should delete parcel successfully")
    void shouldDeleteParcel() {
        // Given
        when(parcelRepository.findById(1L)).thenReturn(Optional.of(parcel));

        // When
        parcelService.deleteParcel(1L);

        // Then
        verify(parcelRepository).delete(parcel);
    }

    @Test
    @DisplayName("Should throw NotFoundException when deleting non-existent parcel")
    void shouldThrowNotFoundExceptionWhenDeletingNonExistentParcel() {
        // Given
        when(parcelRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> parcelService.deleteParcel(999L))
                .isInstanceOf(NotFoundException.class);
    }
}
