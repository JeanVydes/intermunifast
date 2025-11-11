package com.example.services.definitions;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.api.dto.SeatDTOs;
import com.example.domain.repositories.SeatRepository;
import com.example.exceptions.NotFoundException;
import com.example.services.mappers.SeatMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional

public class SeatServiceImpl implements SeatService {

    private final SeatRepository repo;
    private final SeatMapper mapper;

    @Override
    public SeatDTOs.SeatResponse createSeat(SeatDTOs.CreateSeatRequest req) {
        var seat = mapper.toEntity(req);
        return mapper.toResponse(repo.save(seat));
    }

    @Override
    @Transactional(readOnly = true)
    public SeatDTOs.SeatResponse getSeatById(Long id) {
        return repo.findById(id).map(mapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Seat %d not found".formatted(id)));
    }

    @Override
    public void deleteSeat(Long id) {
        var seat = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Seat %d not found".formatted(id)));
        repo.delete(seat);
    }

    @Override
    public SeatDTOs.SeatResponse updateSeat(Long id, SeatDTOs.UpdateSeatRequest req) {
        var seat = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Seat %d not found".formatted(id)));
        mapper.patch(seat, req);
        return mapper.toResponse(repo.save(seat));
    }

    @Override
    @Transactional(readOnly = true) 
    public List<SeatDTOs.SeatResponse> getSeatsByBusId(Long busId) {
        var seats = repo.findByBusId(busId);
        return seats.stream()
                .map(mapper::toResponse)
                .toList();
    }
}
