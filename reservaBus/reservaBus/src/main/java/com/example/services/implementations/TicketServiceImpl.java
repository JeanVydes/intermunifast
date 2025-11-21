package com.example.services.implementations;

import java.time.LocalDateTime;
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
import com.example.domain.enums.FareRulePassengerType;
import com.example.domain.enums.PaymentStatus;
import com.example.domain.enums.TicketStatus;
import com.example.domain.repositories.AccountRepository;
import com.example.domain.repositories.BaggageRepository;
import com.example.domain.repositories.FareRuleRepository;
import com.example.domain.repositories.IncidentRepository;
import com.example.domain.repositories.StopRepository;
import com.example.domain.repositories.TicketRepository;
import com.example.domain.repositories.TripRepository;
import com.example.exceptions.NotFoundException;
import com.example.security.services.AuthenticationService;
import com.example.services.definitions.TicketService;
import com.example.services.extra.SeatAvailabilityService;
import com.example.services.mappers.BaggageMapper;
import com.example.services.mappers.IncidentMapper;
import com.example.services.mappers.TicketMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class TicketServiceImpl implements TicketService {

    private static final String TICKET_NOT_FOUND = "Ticket %d not found";

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
    private final SeatAvailabilityService seatAvailabilityService;

    @Override
    public TicketDTOs.TicketResponse createTicket(TicketDTOs.CreateTicketRequest req) {
        var account = authenticationService.getCurrentAccount();
        var trip = tripRepo.findById(req.tripId())
                .orElseThrow(() -> new NotFoundException("Trip %d not found".formatted(req.tripId())));
        var bus = trip.getBus();

        var fromStop = req.fromStopId().isPresent()
                ? stopRepo.findById(req.fromStopId().get())
                        .orElseThrow(() -> new NotFoundException("Stop %d not found".formatted(req.fromStopId().get())))
                : null;
        var toStop = req.toStopId().isPresent()
                ? stopRepo.findById(req.toStopId().get())
                        .orElseThrow(() -> new NotFoundException("Stop %d not found".formatted(req.toStopId().get())))
                : null;

        if (!seatAvailabilityService.isSeatAvailable(req.tripId(), req.seatNumber(), fromStop, toStop)) {
            var reason = seatAvailabilityService.getAvailabilityConflictReason(req.tripId(), req.seatNumber(), fromStop,
                    toStop);
            var fromName = fromStop != null ? fromStop.getName() : "origin";
            var toName = toStop != null ? toStop.getName() : "destination";
            throw new IllegalStateException("Seat %s is not available for trip %d (segment %s -> %s): %s"
                    .formatted(req.seatNumber(), req.tripId(), fromName, toName, reason));
        }

        // Count occupied seats in the specific segment (fromStop -> toStop)
        int occupiedSeatsInSegment = seatAvailabilityService.getOccupiedSeatsInSegment(req.tripId(), fromStop, toStop);

        if (bus.getCapacity() <= occupiedSeatsInSegment) {
            throw new IllegalStateException("No seats available for trip %d in this segment".formatted(req.tripId()));
        }

        // Determine ticket status based on segment occupation
        // If >= 95% of bus capacity is occupied in this segment, ticket needs approval
        double occupationRate = (double) occupiedSeatsInSegment / bus.getCapacity();
        var status = occupationRate >= 0.95
                ? TicketStatus.PENDING_APPROVAL
                : TicketStatus.CONFIRMED;

        { // debugging
            System.out.println("Bus capacity: " + bus.getCapacity());
            System.out.println("Occupied seats in segment: " + occupiedSeatsInSegment);
            System.out.println("Occupation rate: " + (occupationRate * 100) + "%");
            System.out.println("Ticket status: " + status);
            var fromName = fromStop != null ? fromStop.getName() : "origin";
            var toName = toStop != null ? toStop.getName() : "destination";
            System.out.println("Segment: " + fromName + " -> " + toName);
        }

        var route = trip.getRoute();
        var fareRule = getOrCreateFareRule(route);
        var price = calculatePrice(route, fareRule, req.passengerType());

        var ticket = Ticket.builder()
                .seatNumber(req.seatNumber())
                .trip(trip)
                .fromStop(fromStop)
                .toStop(toStop)
                .paymentMethod(req.paymentMethod())
                .paymentIntentId(req.paymentIntentId())
                .account(accountRepository.getReferenceById(account.getId()))
                .price(price)
                .status(status)
                .paymentStatus(PaymentStatus.PENDING)
                .checkedIn(false)
                .passengerType(req.passengerType())
                .build();

        return mapper.toResponse(repo.save(ticket));
    }

    @Override
    @Transactional(readOnly = true)
    public TicketDTOs.TicketResponse getTicketById(Long id) {
        return repo.findById(id).map(mapper::toResponse)
                .orElseThrow(() -> new NotFoundException(TICKET_NOT_FOUND.formatted(id)));
    }

    @Override
    public void deleteTicket(Long id) {
        var ticket = repo.findById(id)
                .orElseThrow(() -> new NotFoundException(TICKET_NOT_FOUND.formatted(id)));

        // Delete associated baggage first to avoid foreign key constraint violation
        var baggages = baggageRepo.findByTicket_Id(id);
        if (!baggages.isEmpty()) {
            baggageRepo.deleteAll(baggages);
        }

        repo.delete(ticket);
    }

    @Override
    public TicketDTOs.TicketResponse updateTicket(Long id, TicketDTOs.UpdateTicketRequest req) {
        if (req.tripId() != null && !tripRepo.existsById(req.tripId())) {
            throw new NotFoundException("Trip %d not found".formatted(req.tripId()));
        }
        if (req.fromStopId() != null && !stopRepo.existsById(req.fromStopId())) {
            throw new NotFoundException("Stop %d not found".formatted(req.fromStopId()));
        }
        if (req.toStopId() != null && !stopRepo.existsById(req.toStopId())) {
            throw new NotFoundException("Stop %d not found".formatted(req.toStopId()));
        }

        var ticket = repo.findById(id)
                .orElseThrow(() -> new NotFoundException(TICKET_NOT_FOUND.formatted(id)));

        var account = authenticationService.getCurrentAccount();
        if (!ticket.getAccount().getId().equals(account.getId())) {
            throw new NotFoundException(TICKET_NOT_FOUND.formatted(id));
        }

        if (req.passengerType() != null) {
            var route = ticket.getTrip().getRoute();
            var fareRule = getOrCreateFareRule(route);
            ticket.setPrice(calculatePrice(route, fareRule, req.passengerType()));
        }

        mapper.patch(ticket, req);
        return mapper.toResponse(repo.save(ticket));
    }

    @Override
    public TicketDTOs.TicketResponse cancelTicket(Long id) {
        var ticket = repo.findById(id)
                .orElseThrow(() -> new NotFoundException(TICKET_NOT_FOUND.formatted(id)));

        Account account = authenticationService.getCurrentAccount();
        if (!ticket.getAccount().getId().equals(account.getId())) {
            throw new NotFoundException(TICKET_NOT_FOUND.formatted(id));
        }

        Trip trip = ticket.getTrip();
        if (trip.getDepartureAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Cannot cancel ticket for past trips");
        }

        // cannot cancel ticket 5 minutes before trip
        if (trip.getDepartureAt().isBefore(LocalDateTime.now().plusMinutes(5))) {
            throw new IllegalStateException("Cannot cancel ticket within 5 minutes of departure");
        }

        ticket.setStatus(TicketStatus.CANCELLED);
        return mapper.toResponse(repo.save(ticket));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketDTOs.TicketResponse> getAllTickets() {
        return repo.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketDTOs.TicketResponse> searchTickets(Long accountId, String seatNumber) {
        List<Ticket> tickets;
        if (accountId != null && seatNumber != null) {
            tickets = repo.findByAccount_IdAndSeatNumber(accountId, seatNumber);
        } else if (accountId != null) {
            tickets = repo.findByAccount_Id(accountId);
        } else if (seatNumber != null) {
            tickets = repo.findBySeatNumber(seatNumber);
        } else {
            tickets = List.of();
        }
        return tickets.stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BaggageDTOs.BaggageResponse> getBaggagesByTicketId(Long id) {
        if (!repo.existsById(id)) {
            throw new NotFoundException(TICKET_NOT_FOUND.formatted(id));
        }
        return baggageRepo.findByTicket_Id(id).stream()
                .map(baggageMapper::toResponse)
                .toList();
    }

    @Override
    public List<IncidentDTOs.IncidentResponse> getIncidentsByTicketId(Long id) {
        if (!repo.existsById(id)) {
            throw new NotFoundException(TICKET_NOT_FOUND.formatted(id));
        }
        return incidentRepo.findByTicketId(id).stream()
                .map(incidentMapper::toResponse)
                .toList();
    }

    @Override
    public TicketDTOs.TicketResponse markTicketAsPaid(Long id, String paymentIntentId) {
        var ticket = repo.findById(id)
                .orElseThrow(() -> new NotFoundException(TICKET_NOT_FOUND.formatted(id)));

        Account account = authenticationService.getCurrentAccount();
        if (!ticket.getAccount().getId().equals(account.getId())) {
            throw new NotFoundException(TICKET_NOT_FOUND.formatted(id));
        }

        if (ticket.getPaymentStatus() == PaymentStatus.COMPLETED) {
            throw new IllegalStateException("Ticket is already paid");
        }

        if (ticket.getStatus() == TicketStatus.CANCELLED) {
            throw new IllegalStateException("Cannot pay for a cancelled ticket");
        }

        // Update ticket status to SOLD
        ticket.setPaymentStatus(PaymentStatus.COMPLETED);

        // Set payment intent ID if provided
        if (paymentIntentId != null && !paymentIntentId.isBlank()) {
            ticket.setPaymentIntentId(paymentIntentId);
        }

        // Generate QR code if not exists
        if (ticket.getQrCode() == null || ticket.getQrCode().isBlank()) {
            ticket.setQrCode(generateQrCode(ticket));
        }

        return mapper.toResponse(repo.save(ticket));
    }

    @Override
    public List<TicketDTOs.TicketResponse> markMultipleTicketsAsPaid(List<Long> ticketIds, String paymentIntentId) {
        if (ticketIds == null || ticketIds.isEmpty()) {
            throw new IllegalArgumentException("Ticket IDs list cannot be empty");
        }

        Account account = authenticationService.getCurrentAccount();
        List<Ticket> tickets = repo.findAllById(ticketIds);

        if (tickets.size() != ticketIds.size()) {
            throw new NotFoundException("Some tickets were not found");
        }

        // Verify all tickets belong to current user and are unpaid
        validateTicketsForPayment(tickets, account);

        // Update all tickets
        for (Ticket ticket : tickets) {
            ticket.setPaymentStatus(PaymentStatus.COMPLETED);

            if (paymentIntentId != null && !paymentIntentId.isBlank()) {
                ticket.setPaymentIntentId(paymentIntentId);
            }

            if (ticket.getQrCode() == null || ticket.getQrCode().isBlank()) {
                ticket.setQrCode(generateQrCode(ticket));
            }
        }

        List<Ticket> savedTickets = repo.saveAll(tickets);
        return savedTickets.stream()
                .map(mapper::toResponse)
                .toList();
    }

    private void validateTicketsForPayment(List<Ticket> tickets, Account account) {
        for (Ticket ticket : tickets) {
            if (!ticket.getAccount().getId().equals(account.getId())) {
                throw new NotFoundException(TICKET_NOT_FOUND.formatted(ticket.getId()));
            }

            if (ticket.getStatus() == TicketStatus.CANCELLED) {
                throw new IllegalStateException("Cannot pay for cancelled ticket %d".formatted(ticket.getId()));
            }

            if (ticket.getPaymentStatus() == PaymentStatus.COMPLETED) {
                throw new IllegalStateException("Ticket %d is already paid".formatted(ticket.getId()));
            }
        }
    }

    @Override
    public List<TicketDTOs.TicketResponse> getTicketsForCurrentUser(String status) {
        var account = authenticationService.getCurrentAccount();

        if (status != null && !status.isBlank()) {
            try {
                var ticketStatus = TicketStatus.valueOf(status.toUpperCase());
                return repo.findByAccount_IdAndStatus(account.getId(), ticketStatus).stream()
                        .map(mapper::toResponse)
                        .toList();
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid ticket status: " + status);
            }
        }

        return repo.findByAccount_Id(account.getId()).stream()
                .map(mapper::toResponse)
                .toList();
    }

    private FareRule getOrCreateFareRule(Route route) {
        var fareRule = fareRuleRepo.findByRouteId(route.getId());
        if (fareRule != null) {
            return fareRule;
        }

        var basePrice = route.getDistanceKm() * route.getPricePerKm();
        return fareRuleRepo.save(FareRule.builder()
                .route(route)
                .basePrice(basePrice)
                .dynamicPricing(false)
                .childrenDiscount(0.25)
                .seniorDiscount(0.15)
                .studentDiscount(0.10)
                .build());
    }

    private Double calculatePrice(Route route, FareRule fareRule, FareRulePassengerType passengerType) {
        var discount = switch (passengerType) {
            case ADULT -> 0.0;
            case CHILD -> fareRule.getChildrenDiscount();
            case SENIOR -> fareRule.getSeniorDiscount();
            case STUDENT -> fareRule.getStudentDiscount();
        };
        return route.getDistanceKm() * route.getPricePerKm() * (1 - discount);
    }

    private String generateQrCode(Ticket ticket) {
        return "TICKET-%d-%d".formatted(ticket.getId(), System.currentTimeMillis());
    }

    @Override
    public TicketDTOs.TicketResponse approveTicket(Long id) {
        var ticket = repo.findById(id)
                .orElseThrow(() -> new NotFoundException(TICKET_NOT_FOUND.formatted(id)));

        if (ticket.getStatus() != TicketStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Only tickets with PENDING_APPROVAL status can be approved");
        }

        ticket.setStatus(TicketStatus.CONFIRMED);
        return mapper.toResponse(repo.save(ticket));
    }

    @Override
    public TicketDTOs.TicketResponse cancelPendingTicket(Long id) {
        var ticket = repo.findById(id)
                .orElseThrow(() -> new NotFoundException(TICKET_NOT_FOUND.formatted(id)));

        if (ticket.getStatus() != TicketStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Only tickets with PENDING_APPROVAL status can be cancelled");
        }

        ticket.setStatus(TicketStatus.CANCELLED);
        return mapper.toResponse(repo.save(ticket));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketDTOs.TicketResponse> getPendingApprovalTickets() {
        return repo.findByStatus(TicketStatus.PENDING_APPROVAL).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public TicketDTOs.TicketResponse checkInTicket(String qrCode) {
        var ticket = repo.findByQrCode(qrCode);
        if (ticket == null) {
            throw new NotFoundException("Ticket with QR code not found");
        }

        if (ticket.getStatus() == TicketStatus.CANCELLED) {
            throw new IllegalStateException("Cannot check in a cancelled ticket");
        }

        if (ticket.getStatus() == TicketStatus.NO_SHOW) {
            throw new IllegalStateException("Cannot check in a NO_SHOW ticket");
        }

        if (ticket.getPaymentStatus() != PaymentStatus.COMPLETED) {
            throw new IllegalStateException("Cannot check in an unpaid ticket");
        }

        if (ticket.isCheckedIn()) {
            throw new IllegalStateException("Ticket is already checked in");
        }

        var trip = ticket.getTrip();
        var now = LocalDateTime.now();

        // Cannot check in after trip has departed
        if (trip.getDepartureAt().isBefore(now)) {
            throw new IllegalStateException("Cannot check in after trip has departed");
        }

        ticket.setCheckedIn(true);
        ticket.setCheckedInAt(now);
        return mapper.toResponse(repo.save(ticket));
    }

}
