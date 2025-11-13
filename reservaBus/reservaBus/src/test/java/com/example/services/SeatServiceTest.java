package com.example.services;

import com.example.api.dto.SeatDTOs;
import com.example.domain.entities.Bus;
import com.example.domain.entities.Seat;
import com.example.domain.enums.BusStatus;
import com.example.domain.enums.SeatType;
import com.example.domain.repositories.BusRepository;
import com.example.domain.repositories.SeatHoldRepository;
import com.example.domain.repositories.SeatRepository;
import com.example.domain.repositories.TicketRepository;
import com.example.exceptions.NotFoundException;
import com.example.services.definitions.SeatServiceImpl;
import com.example.services.mappers.SeatMapper;
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
@DisplayName("Seat Service Unit Tests")
class SeatServiceTest {

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private SeatMapper seatMapper;

    @Mock
    private StopMapper stopMapper;

    @Mock
    private BusRepository busRepository;

    @Mock
    private SeatHoldRepository seatHoldRepository;

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private SeatServiceImpl seatService;

    private Seat seat;
    private Bus bus;
    private SeatDTOs.SeatResponse seatResponse;
    private SeatDTOs.CreateSeatRequest createRequest;
    private SeatDTOs.UpdateSeatRequest updateRequest;

    @BeforeEach
    void setUp() {
        bus = Bus.builder()
                .id(1L)
                .plate("ABC123")
                .capacity(40)
                .status(BusStatus.ACTIVE)
                .build();

        seat = Seat.builder()
                .id(1L)
                .number("A1")
                .type(SeatType.STANDARD)
                .bus(bus)
                .build();

        seatResponse = new SeatDTOs.SeatResponse(1L, "A1", SeatType.STANDARD, 1L);
        createRequest = new SeatDTOs.CreateSeatRequest("A1", SeatType.STANDARD, 1L);
        updateRequest = new SeatDTOs.UpdateSeatRequest("A2", SeatType.PREFERENTIAL, 1L);
    }

    @Test
    @DisplayName("Should create seat successfully")
    void shouldCreateSeat() {
        // Given
        when(busRepository.findById(1L)).thenReturn(Optional.of(bus));
        when(busRepository.getReferenceById(1L)).thenReturn(bus);
        when(seatRepository.save(any(Seat.class))).thenReturn(seat);
        when(seatMapper.toResponse(seat)).thenReturn(seatResponse);

        // When
        SeatDTOs.SeatResponse result = seatService.createSeat(createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        verify(seatRepository).save(any(Seat.class));
    }

    @Test
    @DisplayName("Should throw NotFoundException when creating seat with non-existent bus")
    void shouldThrowNotFoundExceptionWhenCreatingSeatWithNonExistentBus() {
        // Given
        when(busRepository.findById(999L)).thenReturn(Optional.empty());
        SeatDTOs.CreateSeatRequest invalidRequest = new SeatDTOs.CreateSeatRequest("A1", SeatType.STANDARD, 999L);

        // When & Then
        assertThatThrownBy(() -> seatService.createSeat(invalidRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Bus 999 not found");
    }

    @Test
    @DisplayName("Should get seat by ID successfully")
    void shouldGetSeatById() {
        // Given
        when(seatRepository.findById(1L)).thenReturn(Optional.of(seat));
        when(seatMapper.toResponse(seat)).thenReturn(seatResponse);

        // When
        SeatDTOs.SeatResponse result = seatService.getSeatById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.number()).isEqualTo("A1");
        verify(seatRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw NotFoundException when seat not found by ID")
    void shouldThrowNotFoundExceptionWhenSeatNotFoundById() {
        // Given
        when(seatRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> seatService.getSeatById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Seat 999 not found");
    }

    @Test
    @DisplayName("Should update seat successfully")
    void shouldUpdateSeat() {
        // Given
        when(busRepository.findById(1L)).thenReturn(Optional.of(bus));
        when(seatRepository.findById(1L)).thenReturn(Optional.of(seat));
        when(seatRepository.save(seat)).thenReturn(seat);
        when(seatMapper.toResponse(seat)).thenReturn(seatResponse);

        // When
        SeatDTOs.SeatResponse result = seatService.updateSeat(1L, updateRequest);

        // Then
        assertThat(result).isNotNull();
        verify(seatMapper).patch(seat, updateRequest);
        verify(seatRepository).save(seat);
    }

    @Test
    @DisplayName("Should delete seat successfully")
    void shouldDeleteSeat() {
        // Given
        when(seatRepository.findById(1L)).thenReturn(Optional.of(seat));

        // When
        seatService.deleteSeat(1L);

        // Then
        verify(seatRepository).delete(seat);
    }

    @Test
    @DisplayName("Should get seats by bus ID")
    void shouldGetSeatsByBusId() {
        // Given
        List<Seat> seats = Arrays.asList(seat);
        when(seatRepository.findByBus_Id(1L)).thenReturn(seats);
        when(seatMapper.toResponse(seat)).thenReturn(seatResponse);

        // When
        List<SeatDTOs.SeatResponse> result = seatService.getSeatsByBusId(1L);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).busId()).isEqualTo(1L);
    }
}
