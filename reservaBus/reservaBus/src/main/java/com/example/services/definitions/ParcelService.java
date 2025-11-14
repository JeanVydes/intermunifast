package com.example.services.definitions;

import com.example.api.dto.ParcelDTOs;

public interface ParcelService {
    ParcelDTOs.ParcelResponse createParcel(ParcelDTOs.CreateParcelRequest req);

    ParcelDTOs.ParcelResponse getParcelById(Long id);

    ParcelDTOs.ParcelResponse updateParcel(Long id, ParcelDTOs.UpdateParcelRequest req);

    void deleteParcel(Long id);

    boolean deliverParcel(Long id, ParcelDTOs.ParcelDeliveryRequest req);   
}
