package com.example.services.implementations;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.api.dto.SeatDTOs;
import com.example.domain.entities.Seat;
import com.example.domain.entities.SeatHold;
import com.example.domain.entities.Ticket;
import com.example.domain.repositories.BusRepository;
import com.example.domain.repositories.SeatHoldRepository;
import com.example.domain.repositories.SeatRepository;
import com.example.domain.repositories.TicketRepository;
import com.example.exceptions.NotFoundException;
import com.example.services.definitions.SeatService;
import com.example.services.mappers.SeatMapper;
import com.example.services.mappers.StopMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional

public class SeatServiceImpl implements SeatService {

        private final SeatRepository repo;
        private final SeatMapper mapper;
        private final StopMapper stopMapper;
        private final BusRepository busRepo;
        private final SeatHoldRepository seatHoldRepo;
        private final TicketRepository ticketRepo;

        @Override
        public SeatDTOs.SeatResponse createSeat(SeatDTOs.CreateSeatRequest req) {
                busRepo.findById(req.busId())
                                .orElseThrow(() -> new NotFoundException("Bus %d not found".formatted(req.busId())));
                Seat seat = Seat.builder()
                                .number(req.number())
                                .type(req.type())
                                .bus(busRepo.getReferenceById(req.busId()))
                                .build();
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

                // Update only fields that are present
                req.number().ifPresent(seat::setNumber);
                req.type().ifPresent(seat::setType);
                req.busId().ifPresent(busId -> {
                        var bus = busRepo.findById(busId)
                                        .orElseThrow(() -> new NotFoundException("Bus %d not found".formatted(busId)));
                        seat.setBus(bus);
                });

                return mapper.toResponse(repo.save(seat));
        }

        @Override
        public List<SeatDTOs.SeatReponseFull> getFullSeatsByBusIdAndTripId(Long busId, Long tripId) {
                var seats = repo.findByBus_Id(busId);

                // Get active seat holds for this specific trip
                List<SeatHold> activeSeatHolds = seatHoldRepo.findActiveHoldsByListOfSeatNumbersAndCurrentTimeAndTripId(
                                seats.stream().map(Seat::getNumber).toList(), LocalDateTime.now(), tripId);

                // Get active tickets for this specific trip
                List<Ticket> activeTickets = ticketRepo.findTicketsByListOfSeatNumbersFilteredByTripId(
                                seats.stream().map(Seat::getNumber).toList(), tripId);

                return seats.stream().map(seat -> {
                        var hold = activeSeatHolds.stream()
                                        .filter(h -> h.getSeatNumber().equals(seat.getNumber()))
                                        .findFirst();
                        var ticket = activeTickets.stream()
                                        .filter(t -> t.getSeatNumber().equals(seat.getNumber()))
                                        .findFirst();

                        return new SeatDTOs.SeatReponseFull(
                                        seat.getId(),
                                        seat.getNumber(),
                                        seat.getType(),
                                        seat.getBus().getId(),
                                        hold.map(SeatHold::getId),
                                        hold.map(SeatHold::getExpiresAt),
                                        ticket.map(Ticket::getId),
                                        ticket.map(Ticket::getFromStop).map(stopMapper::toResponse),
                                        ticket.map(Ticket::getToStop).map(stopMapper::toResponse));
                }).toList();
        }

        @Override
        @Transactional(readOnly = true)
        public List<SeatDTOs.SeatResponse> getSeatsByBusId(Long busId) {
                var seats = repo.findByBus_Id(busId);
                return seats.stream()
                                .map(mapper::toResponse)
                                .toList();
        }
}
