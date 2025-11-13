package com.example.services.definitions;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.api.dto.SeatHoldDTOs;
import com.example.domain.entities.SeatHold;
import com.example.domain.entities.Trip;
import com.example.domain.repositories.AccountRepository;
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
    private final AccountRepository accountRepository;
    private final AuthenticationService authenticationService;

    @Override
    public SeatHoldDTOs.SeatHoldResponse reserveSeat(SeatHoldDTOs.CreateSeatHoldRequest req) {
        Long userId = authenticationService.getCurrentAccountId();

        Trip trip = tripRepo.findById(req.tripId())
                .orElseThrow(() -> new NotFoundException("Trip %d not found".formatted(req.tripId())));
        var seatHold = mapper.toEntity(req);

        SeatHold savedSeatHold = repo
                .save(SeatHold.builder().expiresAt(LocalDateTime.now().plusMinutes(10))
                        .seatNumber(seatHold.getSeatNumber()).trip(trip)
                        .account(accountRepository.getReferenceById(userId)).build());
        return mapper.toResponse(savedSeatHold);
    }

    @Override
    @Transactional(readOnly = true)
    public SeatHoldDTOs.SeatHoldResponse getSeatReserveById(Long id) {
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
    public SeatHoldDTOs.SeatHoldResponse updateSeatReserve(Long id, SeatHoldDTOs.UpdateSeatHoldRequest req) {
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
