package com.example.services.definitions;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.api.dto.ParcelDTOs;
import com.example.domain.repositories.ParcelRepository;
import com.example.exceptions.NotFoundException;
import com.example.services.mappers.ParcelMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional

public class ParcelServiceImpl implements ParcelService {

    private final ParcelRepository repo;
    private final ParcelMapper mapper;

    @Override 
    public ParcelDTOs.ParcelResponse createParcel(ParcelDTOs.CreateParcelRequest req) {
        var parcel = mapper.toEntity(req);
        return mapper.toResponse(repo.save(parcel));
    }

    @Override
    @Transactional(readOnly = true) 
    public ParcelDTOs.ParcelResponse getParcelById(Long id) {
        return repo.findById(id).map(mapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Parcel %d not found".formatted(id)));
    }

    @Override
    public void deleteParcel(Long id) {
        var parcel = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Parcel %d not found".formatted(id)));
        repo.delete(parcel);
    }
    
    @Override
    public ParcelDTOs.ParcelResponse updateParcel(Long id, ParcelDTOs.UpdateParcelRequest req) {
        var parcel = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Parcel %d not found".formatted(id)));
        mapper.patch( parcel, req);
        return mapper.toResponse(repo.save(parcel));
    }
}
