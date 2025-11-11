package com.example.services.definitions;

import com.example.api.dto.BaggageDTOs;

public interface BaggageService {
    BaggageDTOs.BaggageResponse createBaggage(BaggageDTOs.CreateBaggageRequest req);

    BaggageDTOs.BaggageResponse getBaggageById(Long id);

    BaggageDTOs.BaggageResponse updateBaggage(Long id, BaggageDTOs.UpdateBaggageRequest req);

    void deleteBaggage(Long id);
}
