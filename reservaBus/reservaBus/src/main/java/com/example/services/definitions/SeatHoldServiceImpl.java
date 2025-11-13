package com.example.services.definitions;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.api.dto.SeatHoldDTOs;
import com.example.domain.entities.SeatHold;
import com.example.domain.entities.Trip;
import com.example.domain.enums.SeatHoldStatus;
import com.example.domain.repositories.SeatHoldRepository;
import com.example.domain.repositories.TripRepository;
import com.example.exceptions.NotFoundException;
import com.example.services.mappers.SeatHoldMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional

public class SeatHoldServiceImpl implements SeatHoldService {

    private final SeatHoldRepository repo;
    private final SeatHoldMapper mapper;
    private final TripRepository tripRepo;

    @Override
    public SeatHoldDTOs.SeatHoldResponse createSeatHold(SeatHoldDTOs.CreateSeatHoldRequest req) {
        Trip trip = tripRepo.findById(req.tripId())
                .orElseThrow(() -> new NotFoundException("Trip %d not found".formatted(req.tripId())));
        var seatHold = mapper.toEntity(req);
        SeatHold savedSeatHold = repo
                .save(SeatHold.builder().status(SeatHoldStatus.HOLD).expiresAt(LocalDateTime.now().plusMinutes(10))
                        .seatNumber(seatHold.getSeatNumber()).trip(trip).build());
        return mapper.toResponse(savedSeatHold);
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
        if (req.tripId() != null) {
            tripRepo.findById(req.tripId())
                    .orElseThrow(() -> new NotFoundException("Trip %d not found".formatted(req.tripId())));

        }
        var seatHold = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("SeatHold %d not found".formatted(id)));
        mapper.patch(seatHold, req);
        return mapper.toResponse(repo.save(seatHold));
    }
}
