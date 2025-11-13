package com.example.services.definitions;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.api.dto.BaggageDTOs;
import com.example.api.dto.IncidentDTOs;
import com.example.api.dto.TicketDTOs;
import com.example.domain.entities.Account;
import com.example.domain.entities.FareRule;
import com.example.domain.entities.Route;
import com.example.domain.entities.Stop;
import com.example.domain.entities.Ticket;
import com.example.domain.entities.Trip;
import com.example.domain.enums.TicketStatus;
import com.example.domain.repositories.AccountRepository;
import com.example.domain.repositories.BaggageRepository;
import com.example.domain.repositories.FareRuleRepository;
import com.example.domain.repositories.IncidentRepository;
import com.example.domain.repositories.StopRepository;
import com.example.domain.repositories.TicketRepository;
import com.example.domain.repositories.TripRepository;
import com.example.exceptions.NotFoundException;
import com.example.services.mappers.BaggageMapper;
import com.example.services.mappers.IncidentMapper;
import com.example.services.mappers.TicketMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional

public class TicketServiceImpl implements TicketService {

    private final TicketRepository repo;
    private final AccountRepository accountRepository;
    private final TicketMapper mapper;
    private final BaggageMapper baggageMapper;
    private final BaggageRepository baggageRepo;
    private final IncidentMapper incidentMapper;
    private final IncidentRepository incidentRepo;
    private final TripRepository tripRepo;
    private final StopRepository stopRepo;
    private final AuthenticationService authenticationService;
    private final FareRuleRepository fareRuleRepo;

    @Override
    public TicketDTOs.TicketResponse createTicket(TicketDTOs.CreateTicketRequest req) {
        Account account = authenticationService.getCurrentAccount();

        Trip trip = tripRepo.findById(req.tripId())
                .orElseThrow(() -> new NotFoundException("Trip %d not found".formatted(req.tripId())));

        Stop fromStop = stopRepo.findById(req.fromStopId())
                .orElseThrow(() -> new NotFoundException("Stop %d not found".formatted(req.fromStopId())));

        Stop toStop = stopRepo.findById(req.toStopId())
                .orElseThrow(() -> new NotFoundException("Stop %d not found".formatted(req.toStopId())));

        Ticket existingTicket = repo.findByTripIdAndSeatNumberAndFromStopIdAndToStopId(req.tripId(), req.seatNumber(),
                req.fromStopId(), req.toStopId());

        if (existingTicket.getStatus().equals(TicketStatus.SOLD)) {
            throw new IllegalStateException(
                    "Seat %s is already sold for trip %d".formatted(req.seatNumber(), req.tripId()));
        }

        Route route = trip.getRoute();
        FareRule fareRule = fareRuleRepo.findByRouteId(route.getId());

        Double discount = 0.0;

        switch (req.passengerType()) {
            case ADULT -> {
                // no discount
            }
            case CHILD -> {
                discount = fareRule.getChildrenDiscount();
            }
            case SENIOR -> {
                discount = fareRule.getSeniorDiscount();
            }
            case STUDENT -> {
                discount = fareRule.getStudentDiscount();
            }
        }

        Double price = route.getDistanceKm() * route.getPricePerKm() * (1 - discount);

        Ticket ticket = Ticket.builder()
                .seatNumber(req.seatNumber())
                .trip(trip)
                .fromStop(fromStop)
                .toStop(toStop)
                .paymentMethod(req.paymentMethod())
                .paymentIntentId(req.paymentIntentId())
                .account(accountRepository.getReferenceById(account.getId()))
                .price(price)
                .status(TicketStatus.SOLD)
                .build();

        return mapper.toResponse(repo.save(ticket));
    }

    @Override
    @Transactional(readOnly = true)
    public TicketDTOs.TicketResponse getTicketById(Long id) {
        return repo.findById(id).map(mapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Ticket %d not found".formatted(id)));
    }

    @Override
    public void deleteTicket(Long id) {
        var ticket = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Ticket %d not found".formatted(id)));
        repo.delete(ticket);
    }

    @Override
    public TicketDTOs.TicketResponse updateTicket(Long id, TicketDTOs.UpdateTicketRequest req) {
        if (req.tripId() != null) {
            tripRepo.findById(req.tripId())
                    .orElseThrow(() -> new NotFoundException("Trip %d not found".formatted(req.tripId())));
        }
        if (req.fromStopId() != null) {
            stopRepo.findById(req.fromStopId())
                    .orElseThrow(() -> new NotFoundException("Stop %d not found".formatted(req.fromStopId())));
        }
        if (req.toStopId() != null) {
            stopRepo.findById(req.toStopId())
                    .orElseThrow(() -> new NotFoundException("Stop %d not found".formatted(req.toStopId())));
        }

        var ticket = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Ticket %d not found".formatted(id)));

        Account account = authenticationService.getCurrentAccount();
        if (ticket.getAccount().getId() != account.getId()) {
            throw new NotFoundException("Ticket %d not found".formatted(id));
        }

        if (req.passengerType() != null) {
            Route route = ticket.getTrip().getRoute();
            FareRule fareRule = fareRuleRepo.findByRouteId(route.getId());

            Double discount = 0.0;

            switch (req.passengerType()) {
                case ADULT -> {
                    // no discount
                }
                case CHILD -> {
                    discount = fareRule.getChildrenDiscount();
                }
                case SENIOR -> {
                    discount = fareRule.getSeniorDiscount();
                }
                case STUDENT -> {
                    discount = fareRule.getStudentDiscount();
                }
            }

            Double price = route.getDistanceKm() * route.getPricePerKm() * (1 - discount);
            ticket.setPrice(price);
        }

        mapper.patch(ticket, req);
        return mapper.toResponse(repo.save(ticket));
    }

    @Override
    public TicketDTOs.TicketResponse cancelTicket(Long id) {
        var ticket = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Ticket %d not found".formatted(id)));

        Account account = authenticationService.getCurrentAccount();
        if (ticket.getAccount().getId() != account.getId()) {
            throw new NotFoundException("Ticket %d not found".formatted(id));
        }

        ticket.setStatus(TicketStatus.CANCELLED);
        return mapper.toResponse(repo.save(ticket));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketDTOs.TicketResponse> searchTickets(Long accountId, String seatNumber) {
        if (accountId == null && seatNumber != null) {
            var tickets = repo.findBySeatNumber(seatNumber);
            return tickets.stream().map(mapper::toResponse).toList();
        }
        if (accountId != null && seatNumber == null) {
            var tickets = repo.findByAccount_Id(accountId);
            return tickets.stream().map(mapper::toResponse).toList();
        }
        if (accountId != null && seatNumber != null) {
            var tickets = repo.findByAccount_IdAndSeatNumber(accountId, seatNumber);
            return tickets.stream().map(mapper::toResponse).toList();
        }
        return List.of();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BaggageDTOs.BaggageResponse> getBaggagesByTicketId(Long id) {
        var ticket = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Ticket %d not found".formatted(id)));
        return baggageRepo.findByTicket_Id(ticket.getId()).stream()
                .map(baggageMapper::toResponse)
                .toList();
    }

    @Override
    public List<IncidentDTOs.IncidentResponse> getIncidentsByTicketId(Long id) {
        var ticket = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Ticket %d not found".formatted(id)));
        return incidentRepo.findByTicketId(ticket.getId()).stream()
                .map(incidentMapper::toResponse)
                .toList();
    }

}
