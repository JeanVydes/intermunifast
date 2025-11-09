package com.example.services.definitions;

import com.example.api.dto.StopDTOs;

public interface StopService {
    StopDTOs.StopResponse createStop(StopDTOs.CreateStopRequest req);

    StopDTOs.StopResponse getStopById(Long id);

    StopDTOs.StopResponse updateStop(Long id, StopDTOs.UpdateStopRequest req);

    void deleteStop(Long id);
}
