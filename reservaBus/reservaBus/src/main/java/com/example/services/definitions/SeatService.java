package com.example.services.definitions;

import java.util.List;

import com.example.api.dto.SeatDTOs;

public interface SeatService {
    SeatDTOs.SeatResponse createSeat(SeatDTOs.CreateSeatRequest req);

    SeatDTOs.SeatResponse getSeatById(Long id);

    SeatDTOs.SeatResponse updateSeat(Long id, SeatDTOs.UpdateSeatRequest req);

    void deleteSeat(Long id);

    List<SeatDTOs.SeatResponse> getSeatsByBusId(Long busId);
}
