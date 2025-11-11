package com.example.services.definitions;

import com.example.api.dto.SeatHoldDTOs;

public interface SeatHoldService {
    SeatHoldDTOs.SeatHoldResponse createSeatHold(SeatHoldDTOs.CreateSeatHoldRequest req);

    SeatHoldDTOs.SeatHoldResponse getSeatHoldById(Long id);

    SeatHoldDTOs.SeatHoldResponse updateSeatHold(Long id, SeatHoldDTOs.UpdateSeatHoldRequest req);

    void deleteSeatHold(Long id);
}
