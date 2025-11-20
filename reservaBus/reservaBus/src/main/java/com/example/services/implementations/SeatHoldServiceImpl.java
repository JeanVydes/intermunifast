package com.example.services.implementations;

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
import com.example.security.services.AuthenticationService;
import com.example.services.extra.ConfigCacheService;
import com.example.services.definitions.SeatHoldService;
import com.example.services.extra.SeatAvailabilityService;
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
        private final ConfigCacheService configCache;

        @Override
        public SeatHoldDTOs.SeatHoldResponse reserveSeat(SeatHoldDTOs.CreateSeatHoldRequest req) {
                Long userId = authenticationService.getCurrentAccountId();
                Trip trip = tripRepo.findById(req.tripId())
                                .orElseThrow(() -> new NotFoundException("Trip %d not found".formatted(req.tripId())));

                Stop fromStop = req.fromStopId().isPresent()
                                ? stopRepo.findById(req.fromStopId().get())
                                                .orElseThrow(() -> new NotFoundException(
                                                                "From Stop %d not found"
                                                                                .formatted(req.fromStopId().get())))
                                : null;

                Stop toStop = req.toStopId().isPresent()
                                ? stopRepo.findById(req.toStopId().get())
                                                .orElseThrow(() -> new NotFoundException(
                                                                "To Stop %d not found".formatted(req.toStopId().get())))
                                : null;

                if (!seatAvailabilityService.isSeatAvailable(req.tripId(), req.seatNumber(), fromStop, toStop)) {
                        String reason = seatAvailabilityService.getAvailabilityConflictReason(
                                        req.tripId(), req.seatNumber(), fromStop, toStop);
                        String fromName = fromStop != null ? fromStop.getName() : "origin";
                        String toName = toStop != null ? toStop.getName() : "destination";
                        throw new IllegalStateException("Seat %s is not available for trip %d (segment %s -> %s): %s"
                                        .formatted(req.seatNumber(), req.tripId(), fromName, toName, reason));
                }

                int holdMinutes = configCache.getMaxSeatHoldMinutes();
                LocalDateTime expiresAt = req.expiresAt() != null ? req.expiresAt()
                                : LocalDateTime.now().plusMinutes(holdMinutes);

                SeatHold savedSeatHold = repo.save(SeatHold.builder()
                                .expiresAt(expiresAt)
                                .seatNumber(req.seatNumber())
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
                req.tripId().ifPresent(tripId -> seatHold.setTrip(tripRepo.findById(tripId)
                                .orElseThrow(() -> new NotFoundException("Trip %d not found".formatted(tripId)))));
                req.fromStopId().ifPresent(fromStopId -> seatHold.setFromStop(stopRepo.findById(fromStopId)
                                .orElseThrow(() -> new NotFoundException(
                                                "From Stop %d not found".formatted(fromStopId)))));
                req.toStopId().ifPresent(toStopId -> seatHold.setToStop(stopRepo.findById(toStopId)
                                .orElseThrow(() -> new NotFoundException("To Stop %d not found".formatted(toStopId)))));

                if (req.seatNumber().isPresent() || req.tripId().isPresent() ||
                                req.fromStopId().isPresent() || req.toStopId().isPresent()) {

                        if (!seatAvailabilityService.isSeatAvailableExcludingHold(
                                        seatHold.getTrip().getId(), seatHold.getSeatNumber(),
                                        seatHold.getFromStop(), seatHold.getToStop(), id)) {
                                String reason = seatAvailabilityService.getAvailabilityConflictReason(
                                                seatHold.getTrip().getId(), seatHold.getSeatNumber(),
                                                seatHold.getFromStop(), seatHold.getToStop());
                                throw new IllegalStateException("Cannot update hold: Seat %s is not available: %s"
                                                .formatted(seatHold.getSeatNumber(), reason));
                        }
                }

                return mapper.toResponse(repo.save(seatHold));
        }
}
