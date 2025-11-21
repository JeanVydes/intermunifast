package com.example.services.definitions;

import java.util.List;

import com.example.api.dto.SeatHoldDTOs;

public interface SeatHoldService {
    SeatHoldDTOs.SeatHoldResponse reserveSeat(SeatHoldDTOs.CreateSeatHoldRequest req);

    SeatHoldDTOs.SeatHoldResponse getSeatReserveById(Long id);

    SeatHoldDTOs.SeatHoldResponse updateSeatReserve(Long id, SeatHoldDTOs.UpdateSeatHoldRequest req);

    void deleteSeatHold(Long id);

    List<SeatHoldDTOs.SeatHoldResponse> getActiveSeatHoldsByTripId(Long tripId);
}
