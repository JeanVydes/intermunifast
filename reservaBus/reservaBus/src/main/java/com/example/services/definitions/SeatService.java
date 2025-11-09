package com.example.services.definitions;

import com.example.api.dto.SeatDTOs;

public interface SeatService {
    SeatDTOs.SeatResponse createSeat(SeatDTOs.CreateSeatRequest req);

    SeatDTOs.SeatResponse getSeatById(Long id);

    SeatDTOs.SeatResponse updateSeat(Long id, SeatDTOs.UpdateSeatRequest req);

    void deleteSeat(Long id);
}
