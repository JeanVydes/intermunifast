package com.example.services.definitions;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.api.dto.BusDTOs;
import com.example.domain.repositories.BusRepository;
import com.example.exceptions.NotFoundException;
import com.example.services.mappers.BusMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
    
public class BusServiceImpl implements BusService {

    private final BusRepository repo;
    private final BusMapper mapper;

    @Override 
    public BusDTOs.BusResponse createBus(BusDTOs.CreateBusRequest req) {
        var bus = mapper.toEntity(req);
        return mapper.toResponse(repo.save(bus));
    }

    @Override
    @Transactional(readOnly = true)
    public BusDTOs.BusResponse getBusById(Long id) {
        return repo.findById(id).map(mapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Bus %d not found".formatted(id)));
    }

    @Override
    public void deleteBus(Long id) {
        var bus = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Bus %d not found".formatted(id)));
        repo.delete(bus);
    }

    @Override
    public BusDTOs.BusResponse updateBus(Long id, BusDTOs.UpdateBusRequest req) {
        var bus = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Bus %d not found".formatted(id)));
        mapper.patch( bus, req);
        return mapper.toResponse(repo.save(bus));
    }

}
