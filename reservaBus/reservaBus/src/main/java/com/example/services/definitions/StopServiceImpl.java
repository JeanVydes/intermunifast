package com.example.services.definitions;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.api.dto.StopDTOs;
import com.example.domain.repositories.StopRepository;
import com.example.exceptions.NotFoundException;
import com.example.services.mappers.StopMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional

public class StopServiceImpl implements StopService {

    private final StopRepository repo;
    private final StopMapper mapper;

    @Override 
    public StopDTOs.StopResponse createStop(StopDTOs.CreateStopRequest req) {
        var stop = mapper.toEntity(req);
        return mapper.toResponse(repo.save(stop));
    }

    @Override
    @Transactional(readOnly = true)
    public StopDTOs.StopResponse getStopById(Long id) {
        return repo.findById(id).map(mapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Stop %d not found".formatted(id)));
    }

    @Override
    public void deleteStop(Long id) {
        var stop = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Stop %d not found".formatted(id)));
        repo.delete(stop);
    }

    @Override
    public StopDTOs.StopResponse updateStop(Long id, StopDTOs.UpdateStopRequest req) {
        var stop = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Stop %d not found".formatted(id)));
        mapper.patch( stop, req);
        return mapper.toResponse(repo.save(stop));
    }

}
