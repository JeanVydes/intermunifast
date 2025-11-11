package com.example.services.definitions;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.api.dto.BaggageDTOs;
import com.example.domain.repositories.BaggageRepository;
import com.example.exceptions.NotFoundException;
import com.example.services.mappers.BaggageMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional

public class BaggageServiceImpl implements BaggageService {

    private final BaggageRepository repo;
    private final BaggageMapper mapper;

    @Override 
    public BaggageDTOs.BaggageResponse createBaggage(BaggageDTOs.CreateBaggageRequest req) {
        var baggage = mapper.toEntity(req);
        return mapper.toResponse(repo.save(baggage));
    }

    @Override
    @Transactional(readOnly = true) 
    public BaggageDTOs.BaggageResponse getBaggageById(Long id) {
        return repo.findById(id).map(mapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Baggage %d not found".formatted(id)));
    }

    @Override
    public void deleteBaggage(Long id) {
        var baggage = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Baggage %d not found".formatted(id)));
        repo.delete(baggage);
    }
    
    @Override
    public BaggageDTOs.BaggageResponse updateBaggage(Long id, BaggageDTOs.UpdateBaggageRequest req) {
        var baggage = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Baggage %d not found".formatted(id)));
        mapper.patch( baggage, req);
        return mapper.toResponse(repo.save(baggage));
    }

}