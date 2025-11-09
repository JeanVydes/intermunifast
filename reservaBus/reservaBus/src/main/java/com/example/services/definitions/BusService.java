package com.example.services.definitions;

import com.example.api.dto.BusDTOs;

public interface BusService {
    BusDTOs.BusResponse createBus(BusDTOs.CreateBusRequest req);

    BusDTOs.BusResponse getBusById(Long id);

    BusDTOs.BusResponse updateBus(Long id, BusDTOs.UpdateBusRequest req);

    void deleteBus(Long id);
}
