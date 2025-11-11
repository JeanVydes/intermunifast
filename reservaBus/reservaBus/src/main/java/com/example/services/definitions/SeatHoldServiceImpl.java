package com.example.services.definitions;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.api.dto.SeatHoldDTOs;
import com.example.domain.repositories.SeatHoldRepository;
import com.example.exceptions.NotFoundException;
import com.example.services.mappers.SeatHoldMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional

public class SeatHoldServiceImpl implements SeatHoldService {

    private final SeatHoldRepository repo;
    private final SeatHoldMapper mapper;

    @Override 
    public SeatHoldDTOs.SeatHoldResponse createSeatHold(SeatHoldDTOs.CreateSeatHoldRequest req) {
        var seatHold = mapper.toEntity(req);
        return mapper.toResponse(repo.save(seatHold));
    }

    @Override
    @Transactional(readOnly = true) 
    public SeatHoldDTOs.SeatHoldResponse getSeatHoldById(Long id) {
        return repo.findById(id).map(mapper::toResponse)
                .orElseThrow(() -> new NotFoundException("SeatHold %d not found".formatted(id)));
    }

    @Override
    public void deleteSeatHold(Long id) {
        var seatHold = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("SeatHold %d not found".formatted(id)));
        repo.delete(seatHold);
    }

    @Override
    public SeatHoldDTOs.SeatHoldResponse updateSeatHold(Long id, SeatHoldDTOs.UpdateSeatHoldRequest req) {
        var seatHold = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("SeatHold %d not found".formatted(id)));
        mapper.patch( seatHold, req);
        return mapper.toResponse(repo.save(seatHold));
    }
}
