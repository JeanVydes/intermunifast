package com.example.services.definitions;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.api.dto.SeatHoldDTOs;
import com.example.domain.entities.SeatHold;
import com.example.domain.entities.Stop;
import com.example.domain.entities.Trip;
import com.example.domain.repositories.AccountRepository;
import com.example.domain.repositories.SeatHoldRepository;
import com.example.domain.repositories.StopRepository;
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
    private final StopRepository stopRepo;
    private final AccountRepository accountRepository;
    private final AuthenticationService authenticationService;
    private final SeatAvailabilityService seatAvailabilityService;

    @Override
    public SeatHoldDTOs.SeatHoldResponse reserveSeat(SeatHoldDTOs.CreateSeatHoldRequest req) {
        Long userId = authenticationService.getCurrentAccountId();

        Trip trip = tripRepo.findById(req.tripId())
                .orElseThrow(() -> new NotFoundException("Trip %d not found".formatted(req.tripId())));

        Stop fromStop = stopRepo.findById(req.fromStopId())
                .orElseThrow(() -> new NotFoundException("From Stop %d not found".formatted(req.fromStopId())));

        Stop toStop = stopRepo.findById(req.toStopId())
                .orElseThrow(() -> new NotFoundException("To Stop %d not found".formatted(req.toStopId())));

        // Validar disponibilidad del asiento para el tramo especificado
        if (!seatAvailabilityService.isSeatAvailable(req.tripId(), req.seatNumber(), fromStop, toStop)) {
            String reason = seatAvailabilityService.getAvailabilityConflictReason(
                    req.tripId(), req.seatNumber(), fromStop, toStop);
            throw new IllegalStateException(
                    "Seat %s is not available for trip %d (segment %s -> %s): %s"
                            .formatted(req.seatNumber(), req.tripId(), fromStop.getName(), toStop.getName(), reason));
        }

        var seatHold = mapper.toEntity(req);

        SeatHold savedSeatHold = repo.save(SeatHold.builder()
                .expiresAt(req.expiresAt() != null ? req.expiresAt() : LocalDateTime.now().plusMinutes(10))
                .seatNumber(seatHold.getSeatNumber())
                .trip(trip)
                .fromStop(fromStop)
                .toStop(toStop)
                .account(accountRepository.getReferenceById(userId))
                .build());
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
        var seatHold = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("SeatHold %d not found".formatted(id)));

        req.seatNumber().ifPresent(seatHold::setSeatNumber);
        req.expiresAt().ifPresent(seatHold::setExpiresAt);

        req.tripId().ifPresent(tripId -> {
            Trip trip = tripRepo.findById(tripId)
                    .orElseThrow(() -> new NotFoundException("Trip %d not found".formatted(tripId)));
            seatHold.setTrip(trip);
        });

        req.fromStopId().ifPresent(fromStopId -> {
            Stop fromStop = stopRepo.findById(fromStopId)
                    .orElseThrow(() -> new NotFoundException("From Stop %d not found".formatted(fromStopId)));
            seatHold.setFromStop(fromStop);
        });

        req.toStopId().ifPresent(toStopId -> {
            Stop toStop = stopRepo.findById(toStopId)
                    .orElseThrow(() -> new NotFoundException("To Stop %d not found".formatted(toStopId)));
            seatHold.setToStop(toStop);
        });

        // Validar disponibilidad si se modificó algún campo crítico
        if (req.seatNumber().isPresent() || req.tripId().isPresent() ||
                req.fromStopId().isPresent() || req.toStopId().isPresent()) {

            Long tripId = seatHold.getTrip().getId();
            String seatNumber = seatHold.getSeatNumber();
            Stop fromStop = seatHold.getFromStop();
            Stop toStop = seatHold.getToStop();

            // Validar disponibilidad excluyendo el hold actual
            if (!seatAvailabilityService.isSeatAvailableExcludingHold(
                    tripId, seatNumber, fromStop, toStop, id)) {
                String reason = seatAvailabilityService.getAvailabilityConflictReason(
                        tripId, seatNumber, fromStop, toStop);
                throw new IllegalStateException(
                        "Cannot update hold: Seat %s is not available for trip %d (segment %s -> %s): %s"
                                .formatted(seatNumber, tripId, fromStop.getName(), toStop.getName(), reason));
            }
        }

        return mapper.toResponse(repo.save(seatHold));
    }
}
